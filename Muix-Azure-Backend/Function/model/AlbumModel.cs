using Azure;
using Azure.Data.Tables;
using Newtonsoft.Json;

namespace Mountainlabs.Model;

public record AlbumModel(
    string Type,
    string ID,
    string Name,
    string MBID,
    string ArtistName,
    string ArtistID,
    string ReleaseDate,
    string ImageURL,
    int? Playcount,
    int? Listeners,
    List<string> Tags,             
    List<TrackItem> Tracks,        
    string DescriptionSummary,
    string DescriptionLong,
    string DescriptionDate
) {
    // helper method to transform a given ArtistTabelModel to a AlbumModel
    // the row parameter is an entry in the table (here "album" as defined in Artist.cs)
    public static AlbumModel GetFromAlbumTableModel(AlbumTableModel row)
    {
        return new
        (
            Type: row.PartitionKey,
            ID: row.RowKey,
            Name: row.Name,
            MBID: row.MBID,
            ArtistName: row.ArtistName,
            ArtistID: row.ArtistID,
            ReleaseDate: row.ReleaseDate,
            ImageURL: row.ImageURL,
            Playcount: row.Playcount,
            Listeners: row.Listeners,
            Tags: ((row.Tags != null) ? JsonConvert.DeserializeObject<List<string>>(row.Tags) : new List<string>()) ?? new List<string>(),
            Tracks: ((row.Tracks != null) ? JsonConvert.DeserializeObject<List<TrackItem>>(row.Tracks) : new List<TrackItem>()) ?? new List<TrackItem>(),
            DescriptionSummary: row.DescriptionSummary,
            DescriptionLong: row.DescriptionLong,
            DescriptionDate: row.DescriptionDate
        );
    }

    public AlbumModel TurnCertainNullValuesToDefaultValues()
    {
        AlbumModel newPlaylistModel = new AlbumModel(
                Type: this.Type,                                //Don't touch as other checks rely on the og values
                ID: this.ID,                                    //Don't touch as other checks rely on the og values
                Name: this.Name,                                //Don't touch as other checks rely on the og values
                ArtistName: this.ArtistName,                    //Don't touch as other checks rely on the og values
                ArtistID: this.ArtistID,                        //Don't touch as other checks rely on the og values
                MBID: this.MBID ?? string.Empty,
                ReleaseDate: this.ReleaseDate ?? string.Empty,
                ImageURL: this.ImageURL ?? string.Empty,
                Playcount: this.Playcount ?? -1,
                Listeners: this.Listeners ?? -1,
                Tags: this.Tags ?? new List<string>(),
                Tracks: this.Tracks ?? new List<TrackItem>(),
                DescriptionSummary: this.DescriptionSummary ?? string.Empty,
                DescriptionLong: this.DescriptionDate ?? string.Empty,
                DescriptionDate: DescriptionDate ?? string.Empty
        );

        return newPlaylistModel;
    }

    public AlbumModel UpdateModelValues(
        string? name = null, 
        string? mbid = null, 
        string? artistName = null, 
        string? artistID = null, 
        string? releaseDate = null,
        int? playcount = null,
        int? listener = null,
        string? imageURL = null,
        List<string>? tags = null,
        List<TrackItem>? tracks = null,
        string? descriptionSummary = null,
        string? descriptionLong = null,
        string? descriptionDate = null
    ) {
        AlbumModel newTrackModel = new AlbumModel(
            Type: this.Type,
            ID: this.ID,
            MBID: mbid ?? this.MBID,
            Name: name ?? this.Name,
            ArtistName: artistName ?? this.ArtistName,
            ArtistID: artistID ?? this.ArtistID,
            ReleaseDate: releaseDate ?? this.ReleaseDate,
            ImageURL: imageURL ?? this.ImageURL,
            Playcount: playcount ?? this.Playcount,
            Listeners: listener ?? this.Listeners,
            Tags: tags ?? this.Tags,
            Tracks: tracks ?? this.Tracks,
            DescriptionSummary: descriptionSummary ?? this.DescriptionSummary,
            DescriptionLong: descriptionLong ?? this.DescriptionLong,
            DescriptionDate: descriptionDate ?? DescriptionDate
        );

        return newTrackModel;
    }
};

public class TrackItem{
    public string Name {get; set;}
    public string TrackID {get; set;}
    public int Duration {get; set;}
    public string ArtistName {get; set;}
    public string ArtistID {get; set;}
}