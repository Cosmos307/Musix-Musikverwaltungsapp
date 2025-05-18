using Mountainlabs.Enum;

namespace Mountainlabs.Model;
using Newtonsoft.Json;
/// <summary>
/// Defines Model of a artist. This is returned by the list and get artist endpoint.
/// The Rowkey is missing on purpose as it is a auto generated global identifier.
/// </summary>
/// <param name="Name">The name of the artist.</param>
/// <param name="MBID">Unique MBID of the artist.</param>
/// <param name="Description">Description of the artist. Should not contain spaces for the get endpoint to work.</param>
/// <param name="ImageURL">URL of an image of this artist</param>
public record ArtistModel(
    string Type,        //== "ARTIST"
    string ID,          //Is unique in combination with type
    string Name,        //Is unique and enforced by the create & update function
    string MBID,
    string DescriptionSummary,
    string DescriptionLong,
    string DescriptionDate,
    string ImageURL,
    int? Listeners,
    List<string>? Tags      
) {
    // helper method to transform a given ArtistTabelModel to a ArtistModel
    // the row parameter is an entry in the table (here "artists" as defined in Artist.cs)
    public static ArtistModel GetFromArtistTableModel(ArtistTableModel row)
    {
        return new
        (
            Type: row.PartitionKey,
            ID: row.RowKey,
            Name: row.Name,
            MBID: row.MBID,
            DescriptionSummary: row.DescriptionSummary,
            DescriptionLong: row.DescriptionLong,
            DescriptionDate: row.DescriptionDate,
            ImageURL: row.ImageURL,
            Listeners: row.Listeners,
            Tags: (row.Tags != null) ? JsonConvert.DeserializeObject<List<string>>(row.Tags) : new List<string>()   
        );
    }

    public ArtistModel TurnCertainNullValuesToDefaultValues()
    {
        ArtistModel newTrackModel = new ArtistModel(
                Type: this.Type,    //Don't touch as other checks rely on the og values
                ID: this.ID,        //Don't touch as other checks rely on the og values
                Name: this.Name,    //Don't touch as other checks rely on the og values
                MBID: this.MBID ?? string.Empty,
                DescriptionSummary: this.DescriptionSummary ?? string.Empty,
                DescriptionLong: this.DescriptionLong ?? string.Empty,
                DescriptionDate: this.DescriptionDate ?? string.Empty,
                ImageURL: this.ImageURL ?? string.Empty,
                Listeners: this.Listeners ?? -1,
                Tags: this.Tags ?? new List<string>()
        );

        return newTrackModel;
    }
};
