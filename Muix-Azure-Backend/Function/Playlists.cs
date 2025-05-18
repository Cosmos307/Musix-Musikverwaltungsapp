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
using Microsoft.Identity.Client;

namespace Mountainlabs.Function
{
    public class Playlists
    {
        private readonly ILogger _logger;
        private readonly TableClient _table;
        static public readonly string itemType = "PLAYLIST";
        private const string modelName = "PlaylistModel";
    
        // Name of the azure storage account table where to create, store, lookup and delete playlists
        private readonly string tableName = "musix";


        public Playlists(ILoggerFactory loggerFactory, TableServiceClient tableService)
        {
            _logger = loggerFactory.CreateLogger<Playlists>();
            // create TableClient for table with name tableName and create table if not exists already
            tableService.CreateTableIfNotExists(tableName);
            _table = tableService.GetTableClient(tableName);
        }

        
        // create new playlist
        [OpenApiOperation(operationId: "createPlaylist", tags: new[] {"playlists"}, Summary = "Create Playlist", Description = "Create a new playlist in the backend.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [Function("HTTPCreatePlaylist")]
        public async Task<HttpResponseData> CreatePlaylist([HttpTrigger(AuthorizationLevel.Function, "post", Route = "playlist")] HttpRequestData request)
        {
          _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request for create playlist endpoint.");

            // deserialize request body into PlaylistModel object
            var createPlaylistReq = await request.ReadFromJsonAsync<PlaylistModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (createPlaylistReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            //Check that the request does not contain an ID as the ID gets set automatically 
            //(give back Error to avoid wrong usage of the function and potential confusion over not "matching" IDs after creation)
            if(createPlaylistReq.ID != string.Empty && createPlaylistReq.ID != null){
                _logger.LogWarning($"Someone tried to create an playlist with an ID! This is not allowed, as the ID gets set here");

                return await HelperFunctions.ErrorAutoSetArgumentProvided(request, argumentName: "ID", itemType: itemType);
            }

            //If playlistName is null or empty => return error
            if(createPlaylistReq.Name == string.Empty || createPlaylistReq.Name == null){
                _logger.LogWarning($"Someone tried to create an playlist with an empty name!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "Name", itemType: itemType);
            }

            //Turn all null values to empty strings and null ints to -1
            createPlaylistReq = createPlaylistReq.TurnCertainNullValuesToDefaultValues();
        
            
            //If an playlist with the same name already exists => give back Error-Model
            var playlistWithSameNameQuery = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType) && row.Name == createPlaylistReq.Name);

            if(playlistWithSameNameQuery.Any())
            {   
                _logger.LogInformation($"Can't create playlist as the playlist name {createPlaylistReq.Name} is already in use!");

                return await ErrorPlaylistNameAlreadyExists(request, createPlaylistReq.Name);
            }

            _logger.LogInformation($"\n The Playlist name {createPlaylistReq.Name} isn't in use by any other playlist yet and can be used \n");

            string generatedID = Guid.NewGuid().ToString("N");

            //Rare case: generated playlist-ID already exists => rerun generation
            while(_table.GetEntityIfExists<PlaylistTableModel>(partitionKey: itemType, rowKey: generatedID).HasValue)
            {
                generatedID = Guid.NewGuid().ToString("N");
            }
            
            //Make sure that each track has an artist ID which corresponds to an acutal artist
            List<TrackItem> updatedTrackItems = new();
            foreach(TrackItem trackItem in createPlaylistReq.Tracks)
            {
                _logger.LogInformation($"\n Trying to find trackName & artistName based on playlist.tracks.track.trackID {trackItem.TrackID} \n");
                
                var fullTrackInfo = _table.GetEntityIfExists<TrackTableModel>(partitionKey: Tracks.itemType, rowKey: trackItem.TrackID);
            
                // return HTTP 404 if no artist found for the given id
                if (!fullTrackInfo.HasValue || fullTrackInfo.Value == null)
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: Tracks.itemType);
                }
                
                trackItem.Name = fullTrackInfo.Value.Name;
                trackItem.Duration = fullTrackInfo.Value.Duration;
                trackItem.ArtistID = fullTrackInfo.Value.ArtistID;
                trackItem.ArtistName = fullTrackInfo.Value.ArtistName;
                updatedTrackItems.Add(trackItem);

                _logger.LogInformation($"\n ArtistName for playlist.tracks.track.artistID {trackItem.ArtistID} is: {trackItem.ArtistName} \n");
            }

            createPlaylistReq = createPlaylistReq.UpdateModelValues(tracks: updatedTrackItems);

            //Serialize Tags to JSON to later pass them on as string to the PlaylistTableModel when adding the new PlaylistTableModel-Entry
            string serializedTags = JsonConvert.SerializeObject(createPlaylistReq.Tags);
            string serializedTracks = JsonConvert.SerializeObject(createPlaylistReq.Tracks);

            // create PlaylistTableModel from value of fields from PlaylistModel and write row to table; partition + row key need to be unique!
            var createTableRow = await _table.AddEntityAsync<PlaylistTableModel>(new()
            {
                RowKey = generatedID,
                Name = createPlaylistReq.Name,
                DescriptionSummary = createPlaylistReq.DescriptionSummary,
                DescriptionDate = createPlaylistReq.DescriptionDate,
                ImageURL = createPlaylistReq.ImageURL,
                Tags = serializedTags,
                Tracks = serializedTracks,
                CreationDate = createPlaylistReq.CreationDate
            });

            // return error if transaction in table storage unsuccessfull
            if (createTableRow.IsError)
            {
                return await HelperFunctions.ErrorTransactionFailed(request);
            }

            // serialize requested PlaylistModel to json and return to client, when request successfull
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(PlaylistModel.GetFromPlaylistTableModel(_table.GetEntity<PlaylistTableModel>(partitionKey: itemType, rowKey: generatedID)));
            return response;
        }


        // get list of all playlists
        [OpenApiOperation(operationId: "listPlaylists", tags: new[] {"playlists"}, Summary = "List Playlists", Description = "Get list of Playlists.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(IList<PlaylistModel>))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("Playlist")]
        public async Task<HttpResponseData> ListPlaylists([HttpTrigger(AuthorizationLevel.Function, "get", Route = "playlist")] HttpRequestData request)
        {
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request for list playlists endpoint.\n");

            // get all Playlists from table storage as list of PlaylistTableModel (this is already deserialized by the TableClient)
            var queryResult = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType));

            if(!queryResult.Any())
            {
                _logger.LogWarning($"The table {tableName} is empty!");

                return await HelperFunctions.ErrorEmptyTableForType(request, tableName: tableName, itemType: itemType);
            }
            
            _logger.LogInformation($"\nTable {_table} has at least one entry :)\n");

            // transform list of PlaylistTableModel objects to list of PlaylistModel
            var resultList = queryResult.Select(row => PlaylistModel.GetFromPlaylistTableModel(row)).ToList();

            // return successfull response
            var response = request.CreateResponse(HttpStatusCode.NotFound);
            await response.WriteAsJsonAsync(resultList);
            response.StatusCode = HttpStatusCode.OK;
            return response;
        }


        // get specific playlist by it's id
        [OpenApiOperation(operationId: "getPlaylist", tags: new[] {"playlists"}, Summary = "Get Playlist by id", Description = "Get an playlist by it's id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the requested playlist")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPGetPlaylist")]
        public async Task<HttpResponseData> GetPlaylist([HttpTrigger(AuthorizationLevel.Function, "get", Route = "playlist/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request to get specific playlist by it's ID: {id} \n");
            
            var queryResult = _table.GetEntityIfExists<PlaylistTableModel>(partitionKey: itemType, rowKey: id);
            
            // return HTTP 404 if no playlist found for the given id
            if (!queryResult.HasValue || queryResult.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // return sucessfull response as PlaylistModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            
            await response.WriteAsJsonAsync(PlaylistModel.GetFromPlaylistTableModel(queryResult.Value));
            return response;
        }


        [OpenApiOperation(operationId: "updatePlaylist", tags: new[] {"playlists"}, Summary = "Update Playlist by ID", Description = "Update an playlist by it's ID.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(UpdatedItems))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.FailedDependency, contentType: "application/json", bodyType: typeof(ErrorModelCascadingUpdate))]
        [Function("HTTPUpdatePlaylist")]
        public async Task<HttpResponseData> UpdatePlaylist([HttpTrigger(AuthorizationLevel.Function, "post", Route = "playlist/update/{id}")] HttpRequestData request)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to update specific playlist by it's id/rowkey");

            // deserialize request body into PlaylistModel object
            PlaylistModel? updatePlaylistReq = await request.ReadFromJsonAsync<PlaylistModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (updatePlaylistReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            _logger.LogInformation($"\n The ID of the playlist that is to be updated: {updatePlaylistReq.ID}  \n");

            //If playlistID is null or empty => return error
            if(updatePlaylistReq.ID == string.Empty || updatePlaylistReq.ID == null){
                _logger.LogWarning($"Someone tried to update an playlist without providing the playlists ID!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "ID", itemType: itemType);
            }

            //Get existing playlist over the given ID
            var playlistToBeUpdated = _table.GetEntityIfExists<PlaylistTableModel>(partitionKey: itemType, rowKey: updatePlaylistReq.ID);
            
            // return HTTP 404 if no playlist found for the given id
            if (!playlistToBeUpdated.HasValue || playlistToBeUpdated.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            _logger.LogInformation("\n Playlist that should be updated exists :) \n");

            //If the playlist gets a new name check this name isn't already in use => else give back errorModel
            if(updatePlaylistReq.Name != playlistToBeUpdated.Value.Name && updatePlaylistReq.Name != null && updatePlaylistReq.Name != string.Empty)
            {
                _logger.LogInformation($"\n The playlist that is to be updated will have a new Name: {updatePlaylistReq.Name} (oldName: {playlistToBeUpdated.Value.Name}) \n");

                var playlistWithSameNameQuery = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(itemType) && !row.RowKey.Equals(updatePlaylistReq.ID) && row.Name.Equals(updatePlaylistReq.Name));

                if(playlistWithSameNameQuery.Any())
                {   
                    _logger.LogInformation($"Can't create playlist as the playlist name {updatePlaylistReq.Name} is already in use!");

                    return await ErrorPlaylistNameAlreadyExists(request, updatePlaylistReq.Name);
                }

                _logger.LogInformation($"\n The new Name {updatePlaylistReq.Name} isn't in use by any other playlist yet and can be used :) \n");
            }


            //Make sure that each track has an artist ID which corresponds to an acutal artist
            
            List<TrackItem> updatedTrackItems = new();
            if(updatePlaylistReq.Tracks != null)
            {
                foreach(TrackItem trackItem in updatePlaylistReq.Tracks)
                {
                _logger.LogInformation($"\n Trying to find trackName & artistName based on playlist.tracks.track.trackID {trackItem.TrackID} \n");
                
                var fullTrackInfo = _table.GetEntityIfExists<TrackTableModel>(partitionKey: Tracks.itemType, rowKey: trackItem.TrackID);
            
                // return HTTP 404 if no artist found for the given id
                if (!fullTrackInfo.HasValue || fullTrackInfo.Value == null)
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: Tracks.itemType);
                }
                
                trackItem.Name = fullTrackInfo.Value.Name;
                trackItem.Duration = fullTrackInfo.Value.Duration;
                trackItem.ArtistID = fullTrackInfo.Value.ArtistID;
                trackItem.ArtistName = fullTrackInfo.Value.ArtistName;
                updatedTrackItems.Add(trackItem);

                _logger.LogInformation($"\n ArtistName for playlist.tracks.track.artistID {trackItem.ArtistID} is: {trackItem.ArtistName} \n");
                }

                updatePlaylistReq = updatePlaylistReq.UpdateModelValues(tracks: updatedTrackItems);
            }

            //Update the values (the model remains not udpated)
            string serializedTags = (updatePlaylistReq.Tags != null) ? JsonConvert.SerializeObject(updatePlaylistReq.Tags) : playlistToBeUpdated.Value.Tags;     //If the incoming tags == null just use the old tags-Value

            string serializedTracks = (updatePlaylistReq.Tracks != null) ? JsonConvert.SerializeObject(updatePlaylistReq.Tracks) : playlistToBeUpdated.Value.Tracks;

            //Update all Values with the requested value, except when its null then just use the old value
            if(updatePlaylistReq.Name != string.Empty && updatePlaylistReq.Name != null)
            {
                playlistToBeUpdated.Value.Name = updatePlaylistReq.Name;
            }
            playlistToBeUpdated.Value.ImageURL = updatePlaylistReq.ImageURL ?? playlistToBeUpdated.Value.ImageURL;
            playlistToBeUpdated.Value.Tags = serializedTags;               //Already checked that tags cannot be null
            playlistToBeUpdated.Value.Tracks = serializedTracks;
            playlistToBeUpdated.Value.DescriptionSummary = updatePlaylistReq.DescriptionSummary ?? playlistToBeUpdated.Value.DescriptionSummary;
            playlistToBeUpdated.Value.DescriptionDate = updatePlaylistReq.DescriptionDate ?? playlistToBeUpdated.Value.DescriptionDate;
            playlistToBeUpdated.Value.CreationDate = updatePlaylistReq.CreationDate ?? playlistToBeUpdated.Value.DescriptionDate;

            //Update the model
            var updatedEntry = _table.UpdateEntity<PlaylistTableModel>(entity: playlistToBeUpdated.Value, ifMatch: playlistToBeUpdated.Value.ETag, mode: TableUpdateMode.Merge);

            // return error if transaction in table storage unsuccessfull
            if (updatedEntry.IsError)
            {
                return await HelperFunctions.ErrorUpdateFailed(request);
            }

            _logger.LogInformation($"\n Sucessfully updated Playlist. Status code: {updatedEntry.Status}\n");

            // return the Artist-Object with it's updated values
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(PlaylistModel.GetFromPlaylistTableModel(_table.GetEntity<PlaylistTableModel>(partitionKey: itemType, rowKey: updatePlaylistReq.ID)));

            return response;
        }

        
        // delete specific playlist by it's ID
        [OpenApiOperation(operationId: "deletePlaylist", tags: new[] {"playlists"}, Summary = "Delete Playlist by ID", Description = "Delete an playlist by it's ID.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the playlist that should be deleted")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(PlaylistModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPDeletePlaylist")]
        public async Task<HttpResponseData> DeletePlaylist([HttpTrigger(AuthorizationLevel.Function, "delete", Route = "playlist/delete/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to delete specific playlist by it's ID: {id}");
            
            var playlist = _table.GetEntityIfExists<PlaylistTableModel>(partitionKey: itemType, rowKey: id);
        
            // return HTTP 404 if no artist found for the given id
            if (!playlist.HasValue || playlist.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // delete the table row
            var queryResult = _table.DeleteEntity(partitionKey: itemType, rowKey: id);
            
            _logger.LogInformation("Playlist deleted: " + queryResult.ToString());

            // return sucessfull response as PlaylistModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            return response;
        }

        private static async Task<HttpResponseData> ErrorPlaylistNameAlreadyExists(HttpRequestData request, string name)
        {
            var errorResponse = request.CreateResponse(statusCode: HttpStatusCode.Conflict);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new ErrorModel(
                Error: "PlaylistNameAlreadyExists",
                ErrorMessage: $"Playlist {name} already exists! Choose a different playlist name."
            ));
            errorResponse.StatusCode = HttpStatusCode.Conflict;
            return errorResponse;         
        }
    }
}
