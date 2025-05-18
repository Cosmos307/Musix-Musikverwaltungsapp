using Azure;
using Azure.Data.Tables;
using Newtonsoft.Json;

namespace Mountainlabs.Model;

public record PlaylistModel(
    string Type,
    string ID,
    string Name,
    string CreationDate,
    string ImageURL,
    List<string> Tags,             
    List<TrackItem> Tracks,        
    string DescriptionSummary,
    string DescriptionDate
) {
    // helper method to transform a given ArtistTabelModel to a PlaylistModel
    // the row parameter is an entry in the table (here "playlist" as defined in Artist.cs)
    public static PlaylistModel GetFromPlaylistTableModel(PlaylistTableModel row)
    {
        return new
        (
            Type: row.PartitionKey,
            ID: row.RowKey,
            Name: row.Name,
            CreationDate: row.CreationDate,
            ImageURL: row.ImageURL,
            Tags: ((row.Tags != null) ? JsonConvert.DeserializeObject<List<string>>(row.Tags) : new List<string>()) ?? new List<string>(),
            Tracks: ((row.Tracks != null) ? JsonConvert.DeserializeObject<List<TrackItem>>(row.Tracks) : new List<TrackItem>()) ?? new List<TrackItem>(),
            DescriptionSummary: row.DescriptionSummary,
            DescriptionDate: row.DescriptionDate
        );
    }

    public PlaylistModel TurnCertainNullValuesToDefaultValues()
    {
        PlaylistModel newPlaylistModel = new PlaylistModel(
                Type: this.Type,                                //Don't touch as other checks rely on the og values
                ID: this.ID,                                    //Don't touch as other checks rely on the og values
                Name: this.Name,                                //Don't touch as other checks rely on the og values
                ImageURL: this.ImageURL ?? string.Empty,
                Tags: this.Tags ?? new List<string>(),
                Tracks: this.Tracks ?? new List<TrackItem>(),
                DescriptionSummary: DescriptionSummary ?? string.Empty,
                DescriptionDate: DescriptionDate ?? string.Empty,
                CreationDate: this.CreationDate ?? string.Empty
        );

        return newPlaylistModel;
    }

        public PlaylistModel UpdateModelValues(
        string? name = null, 
        string? imageURL = null,
        List<string>? tags = null,
        List<TrackItem>? tracks = null,
        string? descriptionSummary = null,
        string? descriptionDate = null,
        string? creationDate = null
    ) {
        PlaylistModel newTrackModel = new PlaylistModel(
            Type: this.Type,
            ID: this.ID,
            Name: name ?? this.Name,
            ImageURL: imageURL ?? this.ImageURL,
            Tags: tags ?? this.Tags,
            Tracks: tracks ?? this.Tracks,
            DescriptionSummary: descriptionSummary ?? DescriptionSummary,
            DescriptionDate: descriptionDate ?? DescriptionDate,
            CreationDate: creationDate ?? this.CreationDate
        );

        return newTrackModel;
    }
};