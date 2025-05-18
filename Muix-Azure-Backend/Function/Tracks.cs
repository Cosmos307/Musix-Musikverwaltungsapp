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
    public class Tracks
    {
        private readonly ILogger _logger;
        private readonly TableClient _table;
    
        public static readonly string itemType = "TRACK";
        private const string modelName = "TrackModel";

        // Name of the azure storage account table where to create, store, lookup and delete tracks
        private readonly string tableName = "musix";

        public Tracks(ILoggerFactory loggerFactory, TableServiceClient tableService)
        {
            _logger = loggerFactory.CreateLogger<Tracks>();
            // create TableClient for table with name tableName and create table if not exists already
            tableService.CreateTableIfNotExists(tableName);
            _table = tableService.GetTableClient(tableName);
        }

        //Create new track based on trackName and artistID/artistName 
        //The artistID is preffered if given, then the artistName is checked meaning that the artistName won't be considered if the artistID is given
        //If the artistID is given but doesnt exist => error; if (artistID is null/empty and) artistName is given but doesnt exist => error or
        [OpenApiOperation(operationId: "createTrack", tags: new[] {"tracks"}, Summary = "Create Track (and Artist)", Description = "Create a new track in the backend. If the specified Artist does not exist, create a new one. If the artist exist add their ID. If the artists ID is specified try to get his name - if that fails return an error.")]        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(TrackModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(TrackModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(TrackModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPCreateTrack")]
        public async Task<HttpResponseData> CreateTrack([HttpTrigger(AuthorizationLevel.Function, "post", Route = "track")] HttpRequestData request)
        {
             _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request for create track endpoint.");

            // deserialize request body into TrackModel object
            var createTrackReq = await request.ReadFromJsonAsync<TrackModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (createTrackReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            //Check that the request does not contain an ID as the ID gets set automatically 
            //(give back Error to avoid wrong usage of the function and potential confusion over not "matching" IDs after creation)
            if(createTrackReq.ID != string.Empty && createTrackReq.ID != null){
                _logger.LogWarning($"Someone tried to create an track with an ID! This is not allowed, as the ID gets set here");

                return await HelperFunctions.ErrorAutoSetArgumentProvided(request, argumentName: "ID", itemType: itemType);
            }

            //If createTrackReq.Name is null or empty => return error
            if(createTrackReq.Name == string.Empty || createTrackReq.Name == null){
                _logger.LogWarning($"Someone tried to create an track with an empty name!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "Name", itemType: itemType);
            }

            //Turn all null values to empty strings
            createTrackReq = createTrackReq.TurnCertainNullValuesToDefaultValues();

            //If artistName is (null or empty) and the trackArtistID is also empty or null => return error
            if((createTrackReq.ArtistName == null || createTrackReq.ArtistName == string.Empty) && (createTrackReq.ArtistID == null || createTrackReq.ArtistID == string.Empty)){
                _logger.LogWarning($"Someone tried to create an track without giving the name or the ID of the artist of the track!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "ArtistNameORArtistID", itemType: itemType);        
            }


            //If trackArtistID is provided find the correct trackArtist name; Else give back error
            if(createTrackReq.ArtistID != null && createTrackReq.ArtistID != string.Empty)
            {
                _logger.LogInformation($"\n Trying to find artistName based on artistID {createTrackReq.ArtistID} \n");
                
                var artist = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: Artists.itemType, rowKey: createTrackReq.ArtistID);
            
                // return HTTP 404 if no artist found for the given id
                if (!artist.HasValue || artist.Value == null)
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: Artists.itemType);
                }

                createTrackReq = createTrackReq.UpdateModelValues(artistName: artist.Value.Name);

                _logger.LogInformation($"\n ArtistName for artistID {createTrackReq.ArtistID} is: {createTrackReq.ArtistName} \n");
            }
            //Else if no trackArtistID is provided, find the artistID by the trackArtistName; Else give back error
            else{

                _logger.LogInformation($"\n Trying to find artistID based on artistName {createTrackReq.ArtistName} \n");

                var artist = _table.Query<ArtistTableModel>(row => row.PartitionKey.Equals(Artists.itemType) && row.Name.Equals(createTrackReq.ArtistName));

                if(!artist.Any())
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: "ARTIST");
                }

                int iteration = 0;
                foreach (ArtistTableModel artistInstance in artist)
                {
                    createTrackReq = createTrackReq.UpdateModelValues(artistID: artistInstance.RowKey);

                    iteration++;
                }

                //Iteration should always be just 1 or else an artist name would double => this shouldn't be possible at all (as the createArtist and updateArtistFunction promise to respect that)
                if(iteration > 1)
                {
                    _logger.LogCritical("\n Two Artists share the same name! How could this happen? \n");
                    return await HelperFunctions.ErrorModelResponse(request, HttpStatusCode.Conflict, error: "CriticalError", errorMessage: $"The artistName {createTrackReq.ArtistName} exists two times! How could this happen?");
                }

                _logger.LogInformation($"\n Artist ID for artistName {createTrackReq.ArtistName} is: {createTrackReq.ArtistID} \n");
            }
            
            //If an track with the same (name and artistName) already exists => give back Error-Model
            var trackWithSameNameQuery = _table.Query<TrackTableModel>(row => row.PartitionKey.Equals(itemType) && row.Name.Equals(createTrackReq.Name) && row.ArtistName.Equals(createTrackReq.ArtistName));

            if(trackWithSameNameQuery.Any())
            {   
                _logger.LogInformation($"\n Can't create track as track name {createTrackReq.Name} of Artist {createTrackReq.ArtistName} already exists! \n");

                return await ErrorTrackAlreadyExists(request, trackName: createTrackReq.Name, artistName: createTrackReq.ArtistName);
            }

            _logger.LogInformation($"\n The Track name {createTrackReq.Name} by Artist {createTrackReq.ArtistName} isn't in use by any other track yet and can be used \n");

            string generatedID = Guid.NewGuid().ToString("N");

            //Rare case: generated track-ID already exists => rerun generation
            while(_table.GetEntityIfExists<TrackTableModel>(partitionKey: itemType, rowKey: generatedID).HasValue)
            {
                generatedID = Guid.NewGuid().ToString("N");
            }
            
            //Serialize Tags to JSON to later pass them on as string to the TrackTableModel when adding the new TrackTableModel-Entry
            string serializedTags = JsonConvert.SerializeObject(createTrackReq.Tags);   

            // create TrackTableModel from value of fields from TrackModel and write row to table; partition + row key need to be unique!
            var createTableRow = await _table.AddEntityAsync<TrackTableModel>(new()
            {
                RowKey = generatedID,
                Name = createTrackReq.Name,
                Duration = createTrackReq.Duration ?? -2,   //-2, as trackModel.TurnNullValuesToEmptyString should already have put the value to -1 if .Listeners was null
                MBID = createTrackReq.MBID,
                ArtistName = createTrackReq.ArtistName ?? "ERROR",  //"ERROR" as the artistName cannot be null as there where null-checks before
                ArtistID = createTrackReq.ArtistID ?? "ERROR",
                ArtistMBID = createTrackReq.ArtistMBID,
                Album = createTrackReq.Album,
                AlbumMBID = createTrackReq.AlbumMBID,
                AlbumPos = createTrackReq.AlbumPos ?? -2,       //-2, as trackModel.TurnNullValuesToEmptyString should already have put the value to -1 if .Listeners was null
                Playcount = createTrackReq.Playcount ?? -2,     //-2, as trackModel.TurnNullValuesToEmptyString should already have put the value to -1 if .Listeners was null
                Listeners = createTrackReq.Listeners ?? -2,     //-2, as trackModel.TurnNullValuesToEmptyString should already have put the value to -1 if .Listeners was null
                ImageURL = createTrackReq.ImageURL,
                Tags = serializedTags,
                DescriptionSummary = createTrackReq.DescriptionSummary,
                DescriptionLong = createTrackReq.DescriptionLong,
                DescriptionDate = createTrackReq.DescriptionDate,
            });

            // return error if transaction in table storage unsuccessfull
            if (createTableRow.IsError)
            {
                var errorResponse = request.CreateResponse(HttpStatusCode.Gone);
                await errorResponse.WriteAsJsonAsync<ErrorModel>(new
                (
                    Error: "TableTransactionError",
                    ErrorMessage: "There was a problem executing the table transaction."
                ));
                return errorResponse;
            }

            // serialize requested TrackModel to json and return to client, when request successfull
            var response = request.CreateResponse(HttpStatusCode.OK);
            //await response.WriteAsJsonAsync(createTrackReq);
            await response.WriteAsJsonAsync(TrackModel.GetFromTrackTableModel(_table.GetEntity<TrackTableModel>(partitionKey: itemType, rowKey: generatedID)));
            return response;
        }


        // get list of all tracks
        [OpenApiOperation(operationId: "listTracks", tags: new[] {"tracks"}, Summary = "List Tracks", Description = "Get list of Tracks.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(IList<TrackModel>))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("Track")]
        public async Task<HttpResponseData> ListTracks([HttpTrigger(AuthorizationLevel.Function, "get", Route = "track")] HttpRequestData request)
        {
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request for list tracks endpoint.\n");

            // get all Tracks from table storage as list of TrackTableModel (this is already deserialized by the TableClient)
            var queryResult = _table.Query<TrackTableModel>(row => row.PartitionKey.Equals(Tracks.itemType));

            if(!queryResult.Any())
            {
                _logger.LogWarning($"The table {tableName} is empty!");

                return await HelperFunctions.ErrorEmptyTableForType(request, tableName: tableName, itemType: itemType);
            }
            
            _logger.LogInformation($"\nTable {_table} has at least one entry :)\n");

            // transform list of TrackTableModel objects to list of TrackModel
            var resultList = queryResult.Select(row => TrackModel.GetFromTrackTableModel(row)).ToList();

            // return successfull response
            var response = request.CreateResponse(HttpStatusCode.NotFound);
            await response.WriteAsJsonAsync(resultList);
            response.StatusCode = HttpStatusCode.OK;
            return response;
        }


        // get specific track by it's id
        [OpenApiOperation(operationId: "getTrack", tags: new[] {"tracks"}, Summary = "Get Track by id", Description = "Get an track by it's id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "ID of the requested track")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(TrackModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPGetTrack")]
        public async Task<HttpResponseData> GetTrack([HttpTrigger(AuthorizationLevel.Function, "get", Route = "track/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"\n [{request.FunctionContext.InvocationId}] Processing request to get specific track by it's ID: {id} \n");
            
            var queryResult = _table.GetEntityIfExists<TrackTableModel>(partitionKey: itemType, rowKey: id);
            
            // return HTTP 404 if no track found for the given id
            if (!queryResult.HasValue || queryResult.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // return sucessfull response as TrackModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            
            await response.WriteAsJsonAsync(TrackModel.GetFromTrackTableModel(queryResult.Value));
            return response;
        }



        //Updates the track based on it's ID; Cannot udpate the ID itself or it's type ("TRACK")
        //Update cascades into Playlists; If update fails for internal server reasons, re-run the update
        //Updating trackName requires a valid artistID or it will fail as aristName (derived from artistID) + trackname are unique
        //To update the artist, give the artistID; artistName won't be accepted and will return an error!
        [OpenApiOperation(operationId: "updateTrack", tags: new[] {"tracks"}, Summary = "Update Track by ID", Description = "Update an track by it's ID.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiRequestBody(contentType: "application/json", bodyType: typeof(TrackModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(UpdatedItems))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.BadRequest, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.Conflict, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.FailedDependency, contentType: "application/json", bodyType: typeof(ErrorModelCascadingUpdate))]
        [Function("HTTPUpdateTrack")]
        public async Task<HttpResponseData> UpdateTrack([HttpTrigger(AuthorizationLevel.Function, "post", Route = "track/update")] HttpRequestData request)
        {   
            bool noNameUpdate = false;

            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to update specific track by it's id/rowkey");

            // deserialize request body into TrackModel object
            TrackModel? updateTrackReq = await request.ReadFromJsonAsync<TrackModel>();

            // if request body cannot be deserialized or is null, return an HTTP 400
            if (updateTrackReq == null) {
                return await HelperFunctions.ErrorCantDeserializeRequest(request, modelName);
            }

            _logger.LogInformation($"\n The ID of the track that is to be updated: {updateTrackReq.ID}  \n");

            //If trackID is null or empty => return error
            if(updateTrackReq.ID == string.Empty || updateTrackReq.ID == null){
                _logger.LogWarning($"Someone tried to update an track without providing the tracks ID!");

                return await HelperFunctions.ErrorFieldEmptyOrNull(request: request, nameRequiredVar: "ID", itemType: itemType);
            }

            //If the artistName is given return error as the artistName is only to be inferred by the artistID and not passed over
            if(updateTrackReq.ArtistName != null && updateTrackReq.ArtistName != string.Empty){
                _logger.LogWarning($"This function expects that the artistName isn't given! It should be null or empty!");

                return await HelperFunctions.ErrorAutoSetArgumentProvided(request, argumentName: "ArtistName", itemType: itemType);
            }

            //If trackName is null or empty => return error
            if(updateTrackReq.Name == string.Empty || updateTrackReq.Name == null){
                noNameUpdate = true;
            }

            //Get existing track over the given ID
            var trackToBeUpdated = _table.GetEntityIfExists<TrackTableModel>(partitionKey: itemType, rowKey: updateTrackReq.ID);
            
            // return HTTP 404 if no track found for the given id
            if (!trackToBeUpdated.HasValue || trackToBeUpdated.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            _logger.LogInformation("\n Track that should be updated exists :) \n");


            //If trackArtistID is provided find the correct trackArtist name; Else give back error
            if(updateTrackReq.ArtistID != null && updateTrackReq.ArtistID != string.Empty)
            {
                _logger.LogInformation($"\n Trying to find artistName based on artistID {updateTrackReq.ArtistID} \n");
                
                var artist = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: Artists.itemType, rowKey: updateTrackReq.ArtistID);
            
                // return HTTP 404 if no artist found for the given id
                if (!artist.HasValue || artist.Value == null)
                {
                    return await HelperFunctions.ErrorModelIDNotFound(request, itemType: Artists.itemType);
                }

                updateTrackReq = updateTrackReq.UpdateModelValues(artistName: artist.Value.Name);

                _logger.LogInformation($"\n ArtistName for artistID {updateTrackReq.ArtistID} is: {updateTrackReq.ArtistName} \n");
            }

            
            //Save the og name for later use
            string ogName = trackToBeUpdated.Value.Name;
            int ogDuration = trackToBeUpdated.Value.Duration;
            string ogArtistName = trackToBeUpdated.Value.ArtistName;

            //If the track gets a new name check this name isn't already in use by the same artist => else give back errorModel
            if(updateTrackReq.Name != trackToBeUpdated.Value.Name && noNameUpdate == false)
            {
                _logger.LogInformation($"\n The track that is to be updated will have a new Name: {updateTrackReq.Name} (oldName: {trackToBeUpdated.Value.Name}) \n");

                if(updateTrackReq.ArtistID == null || updateTrackReq.ArtistID == string.Empty)
                {
                    _logger.LogInformation($"\n Trying to update track.Name without providing an track.ArtistID! This is not possible as the track.Name is unique when taken together with the track.ArtistID. \n");

                    return await HelperFunctions.ErrorFieldEmptyOrNull(request, nameRequiredVar: "ArtistID" , itemType: Artists.itemType);
                }

                var trackWithSameNameQuery = _table.Query<TrackTableModel>(row => row.PartitionKey.Equals(itemType) && !row.RowKey.Equals(updateTrackReq.ID) && row.ArtistID.Equals(updateTrackReq.ArtistID) && row.Name.Equals(updateTrackReq.Name));
                

                if(trackWithSameNameQuery.Any())
                {   
                    _logger.LogInformation($"Can't update track name as the name {updateTrackReq.Name} from artist {updateTrackReq.ArtistName} is already in use!");

                    return await ErrorTrackAlreadyExists(request, trackName: updateTrackReq.Name, artistName: updateTrackReq.ArtistName);
                }

                _logger.LogInformation($"\n The new Name {updateTrackReq.Name} isn't in use by any other track yet and can be used :) \n");
            }

            //Update the values (the model remains not udpated)
            List<string>? cleanTags = updateTrackReq.Tags;
            string serializedTags = (cleanTags != null) ? JsonConvert.SerializeObject(cleanTags) : trackToBeUpdated.Value.Tags;     //If the incoming tags == null just use the old tags-Value

            //Update all Values with the requested value, except when its null then just use the old value
            trackToBeUpdated.Value.Name = updateTrackReq.Name ?? trackToBeUpdated.Value.Name;            //Already checked that Name cannot empty or null
            trackToBeUpdated.Value.Duration = updateTrackReq.Duration ?? trackToBeUpdated.Value.Duration;
            trackToBeUpdated.Value.MBID = updateTrackReq.MBID ?? trackToBeUpdated.Value.MBID;
            trackToBeUpdated.Value.ArtistName = updateTrackReq.ArtistName ?? trackToBeUpdated.Value.ArtistName;
            trackToBeUpdated.Value.ArtistID = updateTrackReq.ArtistID ?? trackToBeUpdated.Value.ArtistID;
            trackToBeUpdated.Value.Album = updateTrackReq.Album ?? trackToBeUpdated.Value.Album;
            trackToBeUpdated.Value.AlbumMBID = updateTrackReq.AlbumMBID ?? trackToBeUpdated.Value.AlbumMBID;
            trackToBeUpdated.Value.AlbumPos = updateTrackReq.AlbumPos ?? trackToBeUpdated.Value.AlbumPos;
            trackToBeUpdated.Value.Playcount = updateTrackReq.Playcount ?? trackToBeUpdated.Value.Playcount;
            trackToBeUpdated.Value.Listeners = updateTrackReq.Listeners ?? trackToBeUpdated.Value.Listeners;
            trackToBeUpdated.Value.ImageURL = updateTrackReq.ImageURL ?? trackToBeUpdated.Value.ImageURL;
            trackToBeUpdated.Value.Tags = serializedTags;               //Already checked that tags cannot be null
            trackToBeUpdated.Value.DescriptionSummary = updateTrackReq.DescriptionSummary ?? trackToBeUpdated.Value.DescriptionSummary;
            trackToBeUpdated.Value.DescriptionLong = updateTrackReq.DescriptionLong ?? trackToBeUpdated.Value.DescriptionLong;
            trackToBeUpdated.Value.DescriptionDate = updateTrackReq.DescriptionDate ?? trackToBeUpdated.Value.DescriptionDate;


            //Update the model
            var updatedEntry = _table.UpdateEntity<TrackTableModel>(entity: trackToBeUpdated.Value, ifMatch: trackToBeUpdated.Value.ETag, mode: TableUpdateMode.Merge);

            // return error if transaction in table storage unsuccessfull
            if (updatedEntry.IsError)
            {
                return await HelperFunctions.ErrorUpdateFailed(request);
            }

            _logger.LogInformation($"\n Sucessfully updated Track. Status code: {updatedEntry.Status}\n");

            //A list of all items that got updated
            List<UpdatedItems> updatedItems = new List<UpdatedItems>(){
                new UpdatedItems(Type: itemType, ID: updateTrackReq.ID)
            };

            
            //Cascade the update if the trackName or the track.artistID changed
            if((updateTrackReq.Name != ogName && noNameUpdate == false) || trackToBeUpdated.Value.ArtistID != ogArtistName)
            {
                //Update all playlists that contain this track (trackName + artistName)     
                //Get all playlists from table storage, as a list of PlaylistTableModel (this is already deserialized by the TableClient)
                var playlists = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType));

                if(playlists.Any())
                {
                    _logger.LogInformation($"\n Start udpating all playlists that contain this track: {trackToBeUpdated.Value.Name} \n");

                    foreach (PlaylistTableModel playlist in playlists)
                    {
                        _logger.LogInformation($"\n Going through playlist {playlist.Name} \n");

                        List<TrackItem>? trackItems = JsonConvert.DeserializeObject<List<TrackItem>>(playlist.Tracks);

                        bool wasTrackUpdated = false;

                        if(trackItems != null && trackItems.Count != 0){
                            List<TrackItem> updatedTrackItems = new();

                            foreach(TrackItem trackItem in trackItems){
                                //if(trackItem.Name == ogName && trackToBeUpdated.Value.Name != ogName){
                                if(trackItem.Name == ogName){
                                    if(trackToBeUpdated.Value.Name != ogName){
                                        trackItem.Name = trackToBeUpdated.Value.Name;
                                        _logger.LogInformation($"\n Try to update playlist.track.'{trackItem.Name}' to {trackToBeUpdated.Value.Name} from playlists '{playlist.Name}' \n");

                                        wasTrackUpdated = true;
                                    }

                                    if(trackToBeUpdated.Value.ArtistName != ogArtistName){
                                        trackItem.ArtistName = trackToBeUpdated.Value.ArtistName; 

                                        _logger.LogInformation($"\n Try to update playlist.track.artistName to {trackToBeUpdated.Value.ArtistName} from Track '{trackItem.Name}' from playlists '{playlist.Name}' \n");
                                        wasTrackUpdated = true;
                                    }

                                    if(trackToBeUpdated.Value.Duration != ogDuration){
                                        trackItem.Duration = trackToBeUpdated.Value.Duration; 

                                        _logger.LogInformation($"\n Try to update playlist.track.Duration to {trackToBeUpdated.Value.Duration} from Track '{trackItem.Name}' from playlists '{playlist.Name}' \n");
                                        wasTrackUpdated = true;
                                    }
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
                                    return await HelperFunctions.ErrorUpdateFailedCascading(request, itemID: trackToBeUpdated.Value.RowKey, updatedItems: updatedItems);
                                }

                                _logger.LogInformation($"\n Sucessfully updated all Tracks in playlist {playlist.Name}; code: {trackItemsUpdated.Status}\n");
                                updatedItems.Add(new UpdatedItems(Type: playlist.PartitionKey, ID: playlist.RowKey));
                            }
                        }
                    }

                    _logger.LogInformation($"\n Successfully updated all playlists with tracks that contained tracks of the {updateTrackReq.Name} (ogName: {ogName})! \n");
                }
            }

            _logger.LogInformation($"\n All tracks updated sucessfully ({updatedItems.Count()-1} in total) \n");

            // return the Artist-Object with it's updated values
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(updatedItems);

            return response;
        }



        // delete specific track by it's id
        [OpenApiOperation(operationId: "deleteTrack", tags: new[] {"tracks"}, Summary = "Delete Track by id", Description = "Delete an track by it's id.")]
        [OpenApiSecurity("function_key", SecuritySchemeType.ApiKey, Name = "code", In = OpenApiSecurityLocationType.Query)]
        [OpenApiParameter(name: "id", In = ParameterLocation.Path, Required = true, Type = typeof(string), Summary = "id of the track that should be deleted")]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.OK, contentType: "application/json", bodyType: typeof(UpdatedItems))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.NotFound, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [OpenApiResponseWithBody(statusCode: HttpStatusCode.FailedDependency, contentType: "application/json", bodyType: typeof(ErrorModel))]
        [Function("HTTPDeleteTrack")]
        public async Task<HttpResponseData> DeleteTrack([HttpTrigger(AuthorizationLevel.Function, "delete", Route = "track/delete/{id}")] HttpRequestData request, string id)
        {   
            _logger.LogInformation($"[{request.FunctionContext.InvocationId}] Processing request to delete specific track by their id: {id}");

            var artist = _table.GetEntityIfExists<ArtistTableModel>(partitionKey: itemType, rowKey: id);
        
            // return HTTP 404 if no artist found for the given id
            if (!artist.HasValue || artist.Value == null)
            {
                return await HelperFunctions.ErrorModelIDNotFound(request, itemType: itemType);
            }

            // delete the table row
            var queryResult = _table.DeleteEntity(partitionKey: itemType, rowKey: id);
            
            _logger.LogInformation("Track deleted: " + queryResult.ToString());


            //Cascading delete: playlist.tracks == id
            //A list of all items that got updated
            List<UpdatedItems> updatedItems = new List<UpdatedItems>();
            var playlists = _table.Query<PlaylistTableModel>(row => row.PartitionKey.Equals(Playlists.itemType));

            if(playlists.Any())
            {
                foreach (PlaylistTableModel playlist in playlists)
                {
                    _logger.LogInformation($"\n Going through playlist {playlist.Name} to delete all tracks with id {id}\n");

                    List<TrackItem>? trackItems = JsonConvert.DeserializeObject<List<TrackItem>>(playlist.Tracks);

                    bool wasTrackDeleted = false;

                    if(trackItems != null && trackItems.Count != 0){
                        List<TrackItem> remainingTrackItems = new();

                        foreach(TrackItem trackItem in trackItems){
                            if(!trackItem.TrackID.Equals(id)){
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
                                return await HelperFunctions.ErrorModelResponse(request, statusCode: HttpStatusCode.FailedDependency, error: "CascadingDeleteArtistError", errorMessage: $"Cascading delete failed! Couldnt delete all tracks that matched the id {id} in playlist {playlist.Name}");
                            }

                            _logger.LogInformation($"\n Sucessfully deleted all Tracks in playlist {playlist.Name} that had the track id {id}; code: {trackItemsUpdated.Status}\n");
                            updatedItems.Add(new UpdatedItems(Type: playlist.PartitionKey, ID: playlist.RowKey));
                        }
                    }
                }

                    _logger.LogInformation($"\n Successfully deleted all orccurnaces of the tracks across all playlists! \n");
                }

            // return sucessfull response as TrackModel
            var response = request.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(updatedItems);
            return response;
        }


        public static async Task<HttpResponseData> ErrorTrackAlreadyExists(HttpRequestData request, string trackName, string artistName)
        {
            var errorResponse = request.CreateResponse(statusCode: HttpStatusCode.Conflict);
            await errorResponse.WriteAsJsonAsync<ErrorModel>(new ErrorModel(
                Error: "TrackAlreadyExists",
                ErrorMessage: $"Track {trackName} of {artistName} already exists! Choose a different artist name."
            ));
            errorResponse.StatusCode = HttpStatusCode.Conflict;
            return errorResponse;         
        }
    }
}
