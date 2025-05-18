using Microsoft.AspNetCore.Rewrite;
using Mountainlabs.Enum;
using Newtonsoft.Json;

namespace Mountainlabs.Model;
public record TrackModel(
    string Type,
    string ID,
    string Name,
    int? Duration,
    string MBID,
    string ArtistName,
    string ArtistID,
    string ArtistMBID,
    string Album,
    string AlbumMBID,
    int? AlbumPos,
    int? Playcount,
    int? Listeners,
    string ImageURL,
    List<string>? Tags,         
    string DescriptionSummary,
    string DescriptionLong,
    string DescriptionDate
) {
    // helper method to transform a given ArtistTabelModel to a AlbumModel
    // the row parameter is an entry in the table (here "track" as defined in Artist.cs)
    public static TrackModel GetFromTrackTableModel(TrackTableModel row)
    {
        return new
        (
            Type: row.PartitionKey,     //== "TRACK"
            ID: row.RowKey,             //Is unique in combination with type
            Name: row.Name,             //Is unique in combination with artistName and enforced by the trackCreate, trackUpdate & artistUdpate function
            Duration: row.Duration,
            MBID: row.MBID,
            ArtistName: row.ArtistName,  //Is unique in combination with artistName and enforced by the trackCreate, trackUpdate & artistUdpate function; Also always the right name for the given artistID (also enforced by all relevant functions)
            ArtistID: row.ArtistID,      //Always matches the artist name
            ArtistMBID: row.ArtistMBID,
            Album: row.Album,
            AlbumMBID: row.AlbumMBID,
            AlbumPos: row.AlbumPos,
            Playcount: row.Playcount,
            Listeners: row.Listeners,
            ImageURL: row.ImageURL,
            Tags: (row.Tags != null) ? JsonConvert.DeserializeObject<List<string>>(row.Tags) : new List<string>(),       
            DescriptionSummary: row.DescriptionSummary,
            DescriptionLong: row.DescriptionLong,
            DescriptionDate: row.DescriptionDate
        );
    }

    public TrackModel TurnCertainNullValuesToDefaultValues()
    {
        TrackModel newTrackModel = new TrackModel(
                Type: this.Type,                                //Don't touch as other checks rely on the og values
                ID: this.ID,                                    //Don't touch as other checks rely on the og values
                Name: this.Name,                                //Don't touch as other checks rely on the og values
                Duration: this.Duration ?? -1,
                MBID: this.MBID ?? string.Empty,
                ArtistName: this.ArtistName,                    //Don't touch as other checks rely on the og values
                ArtistID: this.ArtistID,                        //Don't touch as other checks rely on the og values
                ArtistMBID: this.ArtistMBID ?? string.Empty,
                Album: this.Album ?? string.Empty,
                AlbumMBID: this.AlbumMBID ?? string.Empty,
                AlbumPos: this.AlbumPos ?? -1,
                Playcount: this.Playcount ?? -1,
                Listeners: this.Listeners ?? -1,
                ImageURL: this.ImageURL ?? string.Empty,
                Tags: this.Tags ?? new List<string>(),
                DescriptionSummary: DescriptionSummary ?? string.Empty,
                DescriptionLong: DescriptionLong ?? string.Empty,
                DescriptionDate: DescriptionDate ?? string.Empty
        );

        return newTrackModel;
    }

    public TrackModel UpdateModelValues(
        string? name = null, 
        int? duration = null, 
        string? mbid = null, 
        string? artistName = null, 
        string? artistID = null, 
        string? artistMBID = null, 
        string? album = null,
        string? albumMBID = null,
        int? albumPos = null,
        int? playcount = null,
        int? listener = null,
        string? imageURL = null,
        List<string>? tags = null,
        string? descriptionSummary = null,
        string? descriptionLong = null,
        string? descriptionDate = null
    ) {
        TrackModel newTrackModel = new TrackModel(
            Type: this.Type,
            ID: this.ID,
            Name: name ?? this.Name,
            Duration: duration ?? this.Duration,
            MBID: mbid ?? this.MBID,
            ArtistName: artistName ?? this.ArtistName,
            ArtistID: artistID ?? this.ArtistID,
            ArtistMBID: artistMBID ?? this.ArtistMBID,
            Album: album ?? this.Album,
            AlbumMBID: albumMBID ?? this.AlbumMBID,
            AlbumPos: albumPos ?? this.AlbumPos,
            Playcount: playcount ?? this.Playcount,
            Listeners: listener ?? this.Listeners,
            ImageURL: imageURL ?? this.ImageURL,
            Tags: tags ?? this.Tags,
            DescriptionSummary: descriptionSummary ?? this.DescriptionSummary,
            DescriptionLong: descriptionLong ?? DescriptionLong,
            DescriptionDate: descriptionDate ?? DescriptionDate
        );

        return newTrackModel;
    }
};