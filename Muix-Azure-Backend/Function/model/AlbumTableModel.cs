using Azure;
using Azure.Data.Tables;

namespace Mountainlabs.Model;
public class AlbumTableModel : ITableEntity
{
    //type of item: ALBUM
    public string PartitionKey { get; set; } = "ALBUM";

    //artist unique Name
    public required string RowKey { get; set; } //== Auto generated ID

    // define last insert / update date
    public DateTimeOffset? Timestamp { get; set; }

    // used for cache validation; can be ingored for our use case
    public ETag ETag { get; set; }

    
    public required string Name { get; set; }
    public required string MBID { get; set; }
    public required string ArtistName { get; set; }
    public required string ArtistID { get; set; }
    public required string ReleaseDate { get; set; }
    public required string ImageURL {get; set;}
    public required int Playcount {get; set;}
    public required int Listeners {get; set;}
    public required string Tags {get; set;}     //Is a json serialized List<string>
    public required string Tracks {get; set;}   //Is a json serialized List<Track>; Track member vars: name, trackID, duration, artist, artistID
    public required string DescriptionSummary { get; set; }
    public required string DescriptionLong { get; set; }
    public required string DescriptionDate { get; set; }
}
