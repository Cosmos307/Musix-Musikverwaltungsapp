using System.Net;
using Mountainlabs.Model;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Azure.WebJobs.Extensions.OpenApi.Core.Attributes;
using Microsoft.Azure.WebJobs.Extensions.OpenApi.Core.Enums;
using Microsoft.Extensions.Logging;
using Microsoft.OpenApi.Models;
using System.Linq;
using Azure.Data.Tables;
using Microsoft.Azure.WebJobs.Extensions.OpenApi.Core.Extensions;
using Google.Protobuf.WellKnownTypes;
using Microsoft.AspNetCore.Http;
using Mountainlabs.Enum;
using Newtonsoft.Json;

namespace Mountainlabs.Function
{
    public class Albums
    {
        private readonly ILogger _logger;
        private readonly TableClient _table;
        private const string modelName = "AlbumModel";
        private const string itemType = "ALBUM";
    
        // Name of the azure storage account table where to create, store, lookup and delete albums
        private readonly string tableName = "musix";

        public Albums(ILoggerFactory loggerFactory, TableServiceClient tableService)
        {
            _logger = loggerFactory.CreateLogger<Albums>();
            // create TableClient for table with name tableName and create table if not exists already
            tableService.CreateTableIfNotExists(tableName);
            _table = tableService.GetTableClient(tableName);
        }
        

        // create new album
        [OpenApiOperation(operationId: "createAlbum", tags: new[] {"album"}, Summary = "Create Album", Description = "Create a new album in the backend.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(AlbumModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(AlbumModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(AlbumModel))]
        [Function("HTTPCreateAlbum")]
        public async Task<HttpResponseData> CreateAlbum([HttpTrigger(AuthorizationLevel.Function, "post", Route = "album")] HttpRequestData request)
        {
          _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request for create album endpoint.");

            // deserialize request body into AlbumModel object
            var createAlbumReq = await request.ReadFromJsonAsync<AlbumModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (createAlbumReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            //Check that the request does not contain an ID as the ID gets set automatically 
            //(give back Error to avoid wrong usage of the function and potential confusion over not "matching" IDs after creation)
            if(createAlbumReq.ID != string.Empty && createAlbumReq.ID != null){
                _logger.LogWarning($"Someone tried to create an album with an ID! This is not allowed, as the ID gets set here");

                return await HelperFunctions.ErrorAutoSetArgumentProvided(request, argumentName: "ID", itemType: itemType);
            }

            //If albumName is null or empty => return error
            if(createAlbumReq.Name == string.Empty || createAlbumReq.Name == null){
                _logger.LogWarning($"Someone tried to create an album with an empty name!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "Name", itemType: itemType);
            }

            //Turn all null values to empty strings and null ints to -1
            createAlbumReq = createAlbumReq.TurnCertainNullValuesToDefaultValues();
        
            
            //If an album with the same name already exists => give back Error-Model
            var albumWithSameNameQuery = _table.Query<AlbumTableModel>(row => row.PartitionKey.Equals(Albums.itemType) && row.Name == createAlbumReq.Name);

            if(albumWithSameNameQuery.Any())
            {   
                _logger.LogInformation($"Can't create album as the album name {createAlbumReq.Name} is already in use!");

                return await ErrorAlbumNameAlreadyExists(request, createAlbumReq.Name);
            }

            _logger.LogInformation($"\n The Album name {createAlbumReq.Name} isn't in use by any other album yet and can be used \n");

            string generatedID = Guid.NewGuid().ToString("N");

            //Rare case: generated album-ID already exists => rerun generation
            while(_table.GetEntityIfExists<AlbumTableModel>(partitionKey: itemType, rowKey: generatedID).HasValue)
            {
                generatedID = Guid.NewGuid().ToString("N");
            }
            
            //Make sure that the artist-ID links to an acutal artist; Else give back error
            if(createAlbumReq.ArtistID != null && createAlbumReq.ArtistID != string.Empty)
            {
                _logger.LogInformation($"\n Trying to find artistName based on artistID {createAlbumReq.ArtistID} \n");
                
                var artist = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: Artists.itemType, rowKey: createAlbumReq.ArtistID);
            
                // return HTTP 404 if no artist found for the given id
                if (!artist.HasValue || artist.Value == null)
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: Artists.itemType);
                }

                createAlbumReq = createAlbumReq.UpdateModelValues(artistName: artist.Value.Name);

                _logger.LogInformation($"\n ArtistName for artistID {createAlbumReq.ArtistID} is: {createAlbumReq.ArtistName} \n");
            }
            //Else if no albumArtistID is provided, find the artistID by the albumArtistName; Else give back error
            else{

                _logger.LogInformation($"\n Trying to find artistID based on artistName {createAlbumReq.ArtistName} \n");

                var artist = _table.Query<ArtistTableModel>(row => row.PartitionKey.Equals(Artists.itemType) && row.Name.Equals(createAlbumReq.ArtistName));

                if(!artist.Any())
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: "ARTIST");
                }

                int iteration = 0;
                foreach (ArtistTableModel artistInstance in artist)
                {
                    createAlbumReq = createAlbumReq.UpdateModelValues(artistID: artistInstance.RowKey);

                    iteration++;
                }

                //Iteration should always be just 1 or else an artist name would double => this shouldn't be possible at all (as the createArtist and updateArtistFunction promise to respect that)
                if(iteration > 1)
                {
                    _logger.LogCritical("\n Two Artists share the same name! How could this happen? \n");
                    return await HelperFunctions.ErrorModelResponse(request, HttpStatusCode.Conflict, error: "CriticalError", errorMessage: $"The artistName {createAlbumReq.ArtistName} exists two times! How could this happen?");
                }

                _logger.LogInformation($"\n Artist ID for artistName {createAlbumReq.ArtistName} is: {createAlbumReq.ArtistID} \n");
            }

            //Serialize Tags to JSON to later pass them on as string to the AlbumTableModel when adding the new AlbumTableModel-Entry
            string serializedTags = JsonConvert.SerializeObject(createAlbumReq.Tags);
            string serializedTracks = JsonConvert.SerializeObject(createAlbumReq.Tracks);

            // create AlbumTableModel from value of fields from AlbumModel and write row to table; partition + row key need to be unique!
            var createTableRow = await _table.AddEntityAsync<AlbumTableModel>(new()
            {
                RowKey = generatedID,
                Name = createAlbumReq.Name,
                MBID = createAlbumReq.MBID,
                ArtistName = createAlbumReq.ArtistName,
                ArtistID = createAlbumReq.ArtistID ?? "ERROR",
                ReleaseDate = createAlbumReq.ReleaseDate,
                ImageURL = createAlbumReq.ImageURL,
                Playcount = createAlbumReq.Playcount ?? -2,
                Listeners = createAlbumReq.Listeners ?? -2,
                Tags = serializedTags,
                Tracks = serializedTracks,
                DescriptionSummary = createAlbumReq.DescriptionSummary,
                DescriptionLong = createAlbumReq.DescriptionLong,
                DescriptionDate = createAlbumReq.DescriptionDate,
            });

            // return error if transaction in table storage unsuccessfull
            if (createTableRow.IsError)
            {
                return await HelperFunctions.ErrorTransactionFailed(request);
            }

            // serialize requested AlbumModel to json and return to client, when request successfull
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(AlbumModel.GetFromAlbumTableModel(_table.GetEntity<AlbumTableModel>(partitionKey: itemType, rowKey: generatedID)));
            return response;
        }

        // get list of all album
        [OpenApiOperation(operationId: "listAlbums", tags: new[] {"album"}, Summary = "List Albums", Description = "Get list of Albums.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(IList<AlbumModel>))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("Album")]
        public async Task<HttpResponseData> ListAlbums([HttpTrigger(AuthorizationLevel.Function, "get", Route = "album")] HttpRequestData request)
        {
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request for list album endpoint.\n");

            // get all Albums from table storage as list of AlbumTableModel (this is already deserialized by the TableClient)
            var queryResult = _table.Query<AlbumTableModel>(row => row.PartitionKey.Equals(Albums.itemType));

            if(!queryResult.Any())
            {
                _logger.LogWarning($"The table {tableName} is empty!");

                return await HelperFunctions.ErrorEmptyTableForType(request, tableName: tableName, itemType: itemType);
            }
            
            _logger.LogInformation($"\nTable {_table} has at least one entry :)\n");

            // transform list of AlbumTableModel objects to list of AlbumModel
            var resultList = queryResult.Select(row => AlbumModel.GetFromAlbumTableModel(row)).ToList();

            // return successfull response
            var response = request.CreateResponse(HttpStatusCode.NotFound);
            await response.WriteAsJsonAsync(resultList);
            response.StatusCode = HttpStatusCode.OK;
            return response;
        }


        // get specific album by it's id
        [OpenApiOperation(operationId: "getAlbum", tags: new[] {"album"}, Summary = "Get Album by id", Description = "Get an album by it's id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the requested album")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(AlbumModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPGetAlbum")]
        public async Task<HttpResponseData> GetAlbum([HttpTrigger(AuthorizationLevel.Function, "get", Route = "album/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request to get specific album by it's ID: {id} \n");
            
            var queryResult = _table.GetEntityIfExists<AlbumTableModel>(partitionKey: itemType, rowKey: id);
            
            // return HTTP 404 if no album found for the given id
            if (!queryResult.HasValue || queryResult.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // return sucessfull response as AlbumModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            
            await response.WriteAsJsonAsync(AlbumModel.GetFromAlbumTableModel(queryResult.Value));
            return response;
        }

        // delete specific album by it's ID
        [OpenApiOperation(operationId: "deleteAlbum", tags: new[] {"album"}, Summary = "Delete Album by ID", Description = "Delete an album by it's ID.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the album that should be deleted")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(AlbumModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPDeleteAlbum")]
        public async Task<HttpResponseData> DeleteAlbum([HttpTrigger(AuthorizationLevel.Function, "delete", Route = "album/delete/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to delete specific album by it's ID: {id}");
            
            var album = _table.GetEntityIfExists<AlbumTableModel>(partitionKey: itemType, rowKey: id);
        
            // return HTTP 404 if no artist found for the given id
            if (!album.HasValue || album.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // delete the table row
            var queryResult = _table.DeleteEntity(partitionKey: itemType, rowKey: id);
            
            _logger.LogInformation("Album deleted: " + queryResult.ToString());

            // return sucessfull response as AlbumModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            return response;
        }

        private static async Task<HttpResponseData> ErrorAlbumNameAlreadyExists(HttpRequestData request, string name)
        {
            var errorResponse = request.CreateResponse(statusCode: HttpStatusCode.Conflict);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new ErrorModel(
                Error: "AlbumNameAlreadyExists",
                ErrorMessage: $"Album {name} already exists! Choose a different album name."
            ));
            errorResponse.StatusCode = HttpStatusCode.Conflict;
            return errorResponse;         
        }
    }
}
