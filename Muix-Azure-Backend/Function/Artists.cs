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
using Azure;
using System.Reflection.Metadata.Ecma335;

namespace Mountainlabs.Function
{
    public class Artists
    {
        private readonly ILogger _logger;
        private readonly TableClient _table;
        public static readonly string itemType = "ARTIST";
        private const string modelName = "ArtistModel";
    
        // Name of the azure storage account table where to create, store, lookup and delete artists
        private const string tableName = "musix";

        public Artists(ILoggerFactory loggerFactory, TableServiceClient tableService)
        {
            _logger = loggerFactory.CreateLogger<Artists>();
            // create TableClient for table with name tableName and create table if not exists already
            tableService.CreateTableIfNotExists(tableName);
            _table = tableService.GetTableClient(tableName);
        }

        // create new artist
        [OpenApiOperation(operationId: "createArtist", tags: new[] {"artists"}, Summary = "Create Artist", Description = "Create a new artist in the backend.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(ArtistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(ArtistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(ArtistModel))]
        [Function("HTTPCreateArtist")]
        public async Task<HttpResponseData> CreateArtist([HttpTrigger(AuthorizationLevel.Function, "post", Route = "artist")] HttpRequestData request)
        {
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request for create artist endpoint.");

            // deserialize request body into ArtistModel object
            var createArtistReq = await request.ReadFromJsonAsync<ArtistModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (createArtistReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            //Check that the request does not contain an ID as the ID gets set automatically 
            //(give back Error to avoid wrong usage of the function and potential confusion over not "matching" IDs after creation)
            if(createArtistReq.ID != string.Empty && createArtistReq.ID != null){
                _logger.LogWarning($"Someone tried to create an artist with an ID! This is not allowed, as the ID gets set here");

                return await HelperFunctions.ErrorAutoSetArgumentProvided(request, argumentName: "ID", itemType: itemType);
            }

            //If artistName is null or empty => return error
            if(createArtistReq.Name == string.Empty || createArtistReq.Name == null){
                _logger.LogWarning($"Someone tried to create an artist with an empty name!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "Name", itemType: itemType);
            }

            //Turn all null values to empty strings and null ints to -1
            createArtistReq = createArtistReq.TurnCertainNullValuesToDefaultValues();
        
            
            //If an artist with the same name already exists => give back Error-Model
            var artistWithSameNameQuery = _table.Query<ArtistTableModel>(row => row.PartitionKey.Equals(Artists.itemType) && row.Name.Equals(createArtistReq.Name));

            if(artistWithSameNameQuery.Any())
            {   
                _logger.LogInformation($"Can't create artist as the artist name {createArtistReq.Name} is already in use!");

                return await ErrorArtistNameAlreadyExists(request, createArtistReq.Name);
            }

            _logger.LogInformation($"\n The Artist name {createArtistReq.Name} isn't in use by any other artist yet and can be used \n");

            string generatedID = Guid.NewGuid().ToString("N");

            //Rare case: generated artist-ID already exists => rerun generation
            while(_table.GetEntityIfExists<ArtistTableModel>(partitionKey: itemType, rowKey: generatedID).HasValue)
            {
                generatedID = Guid.NewGuid().ToString("N");
            }
            
            //Serialize Tags to JSON to later pass them on as string to the ArtistTableModel when adding the new ArtistTableModel-Entry
            string serializedTags = JsonConvert.SerializeObject(createArtistReq.Tags);

            // create ArtistTableModel from value of fields from ArtistModel and write row to table; partition + row key need to be unique!
            var createTableRow = await _table.AddEntityAsync<ArtistTableModel>(new()
            {
                RowKey = generatedID,
                Name = createArtistReq.Name,
                MBID = createArtistReq.MBID,
                DescriptionSummary = createArtistReq.DescriptionSummary,
                DescriptionLong = createArtistReq.DescriptionLong,
                DescriptionDate = createArtistReq.DescriptionDate,
                ImageURL = createArtistReq.ImageURL,
                Listeners = createArtistReq.Listeners ?? -2,        //-2, as artistModel.TurnNullValuesToEmptyString should already have put the value to -1 if .Listeners was null
                Tags = serializedTags
            });

            // return error if transaction in table storage unsuccessfull
            if (createTableRow.IsError)
            {
                return await HelperFunctions.ErrorTransactionFailed(request);
            }

            // serialize requested ArtistModel to json and return to client, when request successfull
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(ArtistModel.GetFromArtistTableModel(_table.GetEntity<ArtistTableModel>(partitionKey: itemType, rowKey: generatedID)));
            return response;
        }

        // get list of all artists
        [OpenApiOperation(operationId: "listArtists", tags: new[] {"artists"}, Summary = "List Artists", Description = "Get list of Artists.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(IList<Artists>))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("Artist")]
        public async Task<HttpResponseData> ListArtists([HttpTrigger(AuthorizationLevel.Function, "get", Route = "artist")] HttpRequestData request)
        {
        _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request for list artists endpoint.");

        // get all Artists from table storage as list of ArtistTableModel (this is already deserialized by the TableClient)
        var queryResult = _table.Query<ArtistTableModel>(row => row.PartitionKey.Equals(Artists.itemType));

        if(!queryResult.Any())
        {
            _logger.LogWarning($"The table {tableName} is empty!");

            return await HelperFunctions.ErrorEmptyTableForType(request, tableName: tableName, itemType: itemType);
        }
        
        _logger.LogInformation($"Table {_table} has at least one entry :)");

        // transform list of ArtistTableModel objects to list of ArtistModel
        var resultList = queryResult.Select(row => ArtistModel.GetFromArtistTableModel(row)).ToList();

        // return successfull response
        var response = request.CreateResponse(HttpStatusCode.NotFound);
        await response.WriteAsJsonAsync(resultList);
        response.StatusCode = HttpStatusCode.OK;
        return response;
        }


        // get specific artist by their id
        [OpenApiOperation(operationId: "getArtist", tags: new[] {"artists"}, Summary = "Get Artist by id", Description = "Get an artist by their id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the requested artist")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(ArtistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPGetArtist")]
        public async Task<HttpResponseData> GetArtist([HttpTrigger(AuthorizationLevel.Function, "get", Route = "artist/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to get specific artist by their ID: {id}");
            
            var queryResult = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: itemType, rowKey: id);
            
            // return HTTP 404 if no artist found for the given id
            if (!queryResult.HasValue || queryResult.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // return sucessfull response as ArtistModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            
            await response.WriteAsJsonAsync(ArtistModel.GetFromArtistTableModel(queryResult.Value));
            return response;
        }


        //Updates the artist based on their ID; Cannot udpate the ID itself or it's type ("ARTIST")
        //Update cascades into Tracks & Playlists; If update fails for internal server reasons, re-run the update
        [OpenApiOperation(operationId: "updateArtist", tags: new[] {"artists"}, Summary = "Update Artist by ID", Description = "Update an artist by their ID.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(ArtistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(UpdatedItems))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.FailedDependency, contentType: "application/json", bodyType: typeof(ErrorModelCascadingUpdate))]
        [Function("HTTPUpdateArtist")]
        public async Task<HttpResponseData> UpdateArtist([HttpTrigger(AuthorizationLevel.Function, "post", Route = "artist/update/")] HttpRequestData request)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to update specific artist by their id/rowkey");

            // deserialize request body into ArtistModel object
            ArtistModel? updateArtistReq = await request.ReadFromJsonAsync<ArtistModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (updateArtistReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            _logger.LogInformation($"\n The ID of the artist that is to be updated: {updateArtistReq.ID}  \n");

            //If artistID is null or empty => return error
            if(updateArtistReq.ID == string.Empty || updateArtistReq.ID == null){
                _logger.LogWarning($"Someone tried to update an artist without providing the artists ID!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "ID", itemType: itemType);
            }

            //If artistName is null or empty => return error
            if(updateArtistReq.Name == string.Empty || updateArtistReq.Name == null){
                _logger.LogWarning($"Someone tried to update an artist with an empty name!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "Name", itemType: itemType);
            }

            //Get existing artist over the given ID
            var artistToBeUpdated = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: itemType, rowKey: updateArtistReq.ID);
            
            // return HTTP 404 if no artist found for the given id
            if (!artistToBeUpdated.HasValue || artistToBeUpdated.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            _logger.LogInformation("\n Artist that should be updated exists :) \n");
            
            //Save the og name for later use
            string ogName = artistToBeUpdated.Value.Name;

            //If the artist gets a new name check this name isn't already in use => else give back errorModel
            if(updateArtistReq.Name != artistToBeUpdated.Value.Name)
            {
                _logger.LogInformation($"\n The artist that is to be updated will have a new Name: {updateArtistReq.Name} (oldName: {artistToBeUpdated.Value.Name}) \n");
                
                var artistWithSameNameQuery = _table.Query<ArtistTableModel>(row => row.PartitionKey.Equals(Artists.itemType) && !row.RowKey.Equals(updateArtistReq.ID) && row.Name.Equals(updateArtistReq.Name));

                if(artistWithSameNameQuery.Any())
                {   
                    _logger.LogInformation($"Can't update artist name as the name {updateArtistReq.Name} is already in use!");

                    return await ErrorArtistNameAlreadyExists(request, updateArtistReq.Name);
                }

                _logger.LogInformation($"\n The new Name {updateArtistReq.Name} isn't in use by any other artist yet and can be used :) \n");
            }

            //Update the values (the model remains not udpated)
            List<string>? cleanTags = updateArtistReq.Tags;
            string serializedTags = (cleanTags != null) ? JsonConvert.SerializeObject(cleanTags) : artistToBeUpdated.Value.Tags;

            //Update all Values with the requested value, except when its null then just use the old value
            artistToBeUpdated.Value.Name = updateArtistReq.Name;            //Already checked that Name cannot empty or null
            artistToBeUpdated.Value.MBID = updateArtistReq.MBID ?? artistToBeUpdated.Value.MBID;
            artistToBeUpdated.Value.DescriptionSummary = updateArtistReq.DescriptionSummary ?? artistToBeUpdated.Value.DescriptionSummary;
            artistToBeUpdated.Value.DescriptionLong = updateArtistReq.DescriptionLong ?? artistToBeUpdated.Value.DescriptionLong;
            artistToBeUpdated.Value.DescriptionDate = updateArtistReq.DescriptionDate ?? artistToBeUpdated.Value.DescriptionDate;
            artistToBeUpdated.Value.ImageURL = updateArtistReq.ImageURL ?? artistToBeUpdated.Value.ImageURL;
            artistToBeUpdated.Value.Listeners = updateArtistReq.Listeners ?? artistToBeUpdated.Value.Listeners;
            artistToBeUpdated.Value.Tags = serializedTags;

            //Update the model
            var updatedEntry = _table.UpdateEntity<ArtistTableModel>(entity: artistToBeUpdated.Value, ifMatch: artistToBeUpdated.Value.ETag, mode: TableUpdateMode.Merge);

            // return error if transaction in table storage unsuccessfull
            if (updatedEntry.IsError)
            {
                return await HelperFunctions.ErrorUpdateFailed(request);
            }

            _logger.LogInformation($"\n Sucessfully updated Artist. Status code: {updatedEntry.Status}\n");

            //A list of all items that got updated
            List<UpdatedItems> updatedItems = new List<UpdatedItems>(){
                new UpdatedItems(Type: itemType, ID: updateArtistReq.ID)
            };

            //Cascade the update if the artistName changed
            if(updateArtistReq.Name != ogName)
            {
                //Update all tracks of this artist
                //Get all tracks from table storage that have the same artistID, as a list of TrackTableModel (this is already deserialized by the TableClient)
                var tracksOfArtist = _table.Query<TrackTableModel>(row => row.PartitionKey.Equals(Tracks.itemType) && row.ArtistID.Equals(updateArtistReq.ID));
                
                if(tracksOfArtist.Any())
                {   
                    _logger.LogInformation($"Start udpating all tracks of this artist to reflect their new name...");

                    foreach (TrackTableModel track in tracksOfArtist)
                    {
                        if(track.ArtistID == updateArtistReq.ID) //&& updateArtistReq.Name != ogName
                        {
                            track.ArtistName = updateArtistReq.Name;

                            var updatedTrack = _table.UpdateEntity<TrackTableModel>(entity: track, ifMatch: track.ETag, mode: TableUpdateMode.Merge);

                            // return error if transaction in table storage unsuccessfull
                            if (updatedTrack.IsError)
                            {
                                return await HelperFunctions.ErrorUpdateFailedCascading(request, itemID: updateArtistReq.ID, updatedItems: updatedItems);
                            }

                            updatedItems.Add(new UpdatedItems(Type: track.PartitionKey, ID: track.RowKey));

                            _logger.LogInformation($"\n Sucessfully updated the artistname of Track {track.Name} to {updateArtistReq.Name}; code: {updatedEntry.Status}\n");
                        }
                    }
                }

                //Update all the tracks that are inside of a playlists that are made by this artist
                //Get all playlists from table storage, as a list of PlaylistTableModel (this is already deserialized by the TableClient)
                var playlists = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType));

                if(playlists.Any())
                {
                    _logger.LogInformation($"\n Start udpating all playlist.tracks.artistName of this artist... \n");

                    foreach (PlaylistTableModel playlist in playlists)
                    {
                        _logger.LogInformation($"\n Going through playlist {playlist.Name} \n");

                        List<TrackItem>? trackItems = JsonConvert.DeserializeObject<List<TrackItem>>(playlist.Tracks);

                        bool wasTrackUpdated = false;

                        if(trackItems != null && trackItems.Count != 0)
                        {
                            List<TrackItem> updatedTrackItems = new();

                            foreach(TrackItem trackItem in trackItems){
                                if(trackItem.ArtistName == ogName && updateArtistReq.Name != ogName)
                                {
                                    trackItem.ArtistName = updateArtistReq.Name;

                                    _logger.LogInformation($"\n Updating playlist.track.artisName to {updateArtistReq.Name} from Track '{trackItem.Name}' from playlists '{playlist.Name}' \n");
                                    wasTrackUpdated = true;
                                }

                                updatedTrackItems.Add(trackItem);
                            }

                            if(wasTrackUpdated)
                            {
                                playlist.Tracks = JsonConvert.SerializeObject(updatedTrackItems);
                                
                                var trackItemsUpdated = _table.UpdateEntity<PlaylistTableModel>(entity: playlist, ifMatch: playlist.ETag, mode: TableUpdateMode.Merge); 
                                
                                // return error if transaction in table storage unsuccessfull
                                if (trackItemsUpdated.IsError)
                                {
                                    return await HelperFunctions.ErrorUpdateFailedCascading(request, itemID: updateArtistReq.ID, updatedItems: updatedItems);
                                }

                                _logger.LogInformation($"\n Sucessfully updated all Tracks in playlist {playlist.Name}; code: {trackItemsUpdated.Status}\n");
                                updatedItems.Add(new UpdatedItems(Type: playlist.PartitionKey, ID: playlist.RowKey));
                            }
                        }
                    }

                    _logger.LogInformation($"\n Successfully updated all playlists with tracks of artist {updateArtistReq.Name}! \n"); 
                }
            }

            _logger.LogInformation($"\n All tracks updated sucessfully ({updatedItems.Count()-1} in total) \n");

            // return the Artist-Object with it's updated values
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(updatedItems);

            return response;
        }

        // delete specific artist by their id and all of their tracks and their tracks that are in playlists
        [OpenApiOperation(operationId: "deleteArtist", tags: new[] {"artists"}, Summary = "Delete Artist by id", Description = "Delete an artist by their id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "id of the artist that should be deleted")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(UpdatedItems))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.FailedDependency, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPDeleteArtist")]
        public async Task<HttpResponseData> DeleteArtist([HttpTrigger(AuthorizationLevel.Function, "delete", Route = "artist/delete/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to delete specific artist by their id: {id}");
            
            var artist = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: itemType, rowKey: id);
        
            // return HTTP 404 if no artist found for the given id
            if (!artist.HasValue || artist.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }
            
            string artistName = artist.Value.Name;

            // delete the table row
            var queryResult = _table.DeleteEntity(partitionKey: itemType, rowKey: id);
            
            _logger.LogInformation($"Artist {artistName} deleted: " + queryResult.ToString());


            List<UpdatedItems> updatedItems = new List<UpdatedItems>();
            //Cascading delete: delete all Tracks from that artist
            var tracksFromTheArtist = _table.Query<TrackTableModel>(row => row.PartitionKey.Equals(Tracks.itemType) && row.ArtistID.Equals(id));

            foreach(TrackTableModel track in tracksFromTheArtist)
            {
                // delete the track
                var queryTrackResult = _table.DeleteEntity(partitionKey: Tracks.itemType, rowKey: track.RowKey);
                
                _logger.LogInformation($"Track {track.Name} by {artistName} deleted: " + queryTrackResult.ToString());
            }

            //Cascading delete: delete all playlists.tracks from that artist
            var playlists = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType));

            if(playlists.Any())
            {
                foreach (PlaylistTableModel playlist in playlists)
                {
                    _logger.LogInformation($"\n Going through playlist {playlist.Name} to delete all tracks by the artist {artistName}\n");

                    List<TrackItem>? trackItems = JsonConvert.DeserializeObject<List<TrackItem>>(playlist.Tracks);

                    bool wasTrackDeleted = false;

                    if(trackItems != null && trackItems.Count != 0)
                    {
                        List<TrackItem> remainingTrackItems = new();

                        foreach(TrackItem trackItem in trackItems){
                            if(trackItem.ArtistID != id)
                            {
                                remainingTrackItems.Add(trackItem);
                            }
                            else
                            {
                                wasTrackDeleted = true;
                            }
                        }

                        if(wasTrackDeleted)
                        {
                            playlist.Tracks = JsonConvert.SerializeObject(remainingTrackItems);
                            
                            var trackItemsUpdated = _table.UpdateEntity<PlaylistTableModel>(entity: playlist, ifMatch: playlist.ETag, mode: TableUpdateMode.Merge);
                            
                            // return error if transaction in table storage unsuccessfull
                            if (trackItemsUpdated.IsError)
                            {
                                return await HelperFunctions.ErrorModelResponse(request, statusCode: HttpStatusCode.FailedDependency, error: "CascadingDeleteArtistError", errorMessage: $"Cascading delete failed! Couldnt delete all tracks by the artist {artistName} in playlist {playlist.Name}");
                            }
                            
                            updatedItems.Add(new UpdatedItems(Type: playlist.PartitionKey, ID: playlist.RowKey));

                            _logger.LogInformation($"\n Sucessfully deleted all tracks from artist {artistName} in playlist {playlist.Name}; code: {trackItemsUpdated.Status}\n");
                        }
                    }
                }
            }

            // return sucessfull response as ArtistModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(updatedItems);
            return response;
        }


        public static async Task<HttpResponseData> ErrorArtistNameAlreadyExists(HttpRequestData request, string name)
        {
            var errorResponse = request.CreateResponse(statusCode: HttpStatusCode.Conflict);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new ErrorModel(
                Error: "ArtistNameAlreadyExists",
                ErrorMessage: $"Artist {name} already exists! Choose a different artist name."
            ));
            errorResponse.StatusCode = HttpStatusCode.Conflict;
            return errorResponse;         
        }
    }

    public static class HelperFunctions
    {
        //Error-Model Functions
        public static async Task<HttpResponseData> ErrorCantDeserializeRequest(HttpRequestData request, string modelName)
        {
            var errorResponse = request.CreateResponse(HttpStatusCode.BadRequest);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new
            (
                Error: "Bad request",
                ErrorMessage: $"Couldnt map the incoming data to {modelName}!"
            ));
            errorResponse.StatusCode = HttpStatusCode.BadRequest;
            return errorResponse;
        }        

        public static async Task<HttpResponseData> ErrorModelResponse(HttpRequestData request, HttpStatusCode statusCode, string error, string errorMessage)
        {
            var errorResponse = request.CreateResponse(statusCode: statusCode);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new ErrorModel(
                Error: error,
                ErrorMessage: errorMessage
            ));
            errorResponse.StatusCode = statusCode;
            return errorResponse;
        }

        //Cascading update fails: give back the artist ID to be able to re-run the udpate for that artist and hopefully complete the update; Also returns all items that have finished their update process
        public static async Task<HttpResponseData> ErrorUpdateFailedCascading(HttpRequestData request, string itemID, List<UpdatedItems> updatedItems)
        {
            var errorResponse = request.CreateResponse(statusCode: HttpStatusCode.FailedDependency);
            await errorResponse.WriteAsJsonAsync<ErrorModelCascadingUpdate>(new ErrorModelCascadingUpdate(
                ArtistID: itemID,
                FinishedUpdatedItems: updatedItems
            ));
            errorResponse.StatusCode = HttpStatusCode.FailedDependency;
            return errorResponse;
        }

        public static async Task<HttpResponseData> ErrorEmptyTableForType(HttpRequestData request, string tableName, string itemType)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.NotFound, error: $"EmptyTableFor{itemType}", errorMessage: $"Table {tableName} does not contain any {itemType}");
        }

        public static async Task<HttpResponseData> ErrorFieldEmptyOrNull(HttpRequestData request, string nameRequiredVar, string itemType)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.BadRequest, error: $"Empty{nameRequiredVar}", errorMessage: $"{itemType}.{nameRequiredVar} can not be empty or null!");
        }

        public static async Task<HttpResponseData> ErrorTransactionFailed(HttpRequestData request)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.InternalServerError, error: "TableTransactionError", errorMessage: $"There was a problem executing the table transaction.");
        }

        public static async Task<HttpResponseData> ErrorUpdateFailed(HttpRequestData request)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.InternalServerError, error: "TableUpdateError", errorMessage: $"There was a problem executing the table update.");
        }

        public static async Task<HttpResponseData> ErrorTypeMatching(HttpRequestData request, string givenType, string requiredType)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.BadRequest, error: "TypeMatchingError", errorMessage: $"The given type {givenType} of this object does not match the required type {requiredType}!");
        }

        //Give back error model when an argument was provided that should have been empty as it will be set automatically
        public static async Task<HttpResponseData> ErrorAutoSetArgumentProvided(HttpRequestData request, string argumentName, string itemType)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.BadRequest, error: "AutoSetArgumentProvidedError", errorMessage: $"{itemType}.{argumentName} should not be given! It gets set automatically");
        }

        
        public static async Task<HttpResponseData> ErrorModelIDNotFound(HttpRequestData request, string itemType)
        {
            return await HelperFunctions.ErrorModelResponse(request: request, statusCode: HttpStatusCode.NotFound, error: $"{itemType}NotFound", errorMessage: $"{itemType}.ID not found!");
        }
    }
}
