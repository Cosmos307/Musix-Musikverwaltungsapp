using Azure;
using Azure.Data.Tables;

namespace Mountainlabs.Model;
public class PlaylistTableModel : ITableEntity
{
    //type of item: PLAYLIST
    public string PartitionKey { get; set; } = "PLAYLIST";

    //Playlist unique ID (auto generated)
    public required string RowKey { get; set; }

    // define last insert / update date
    public DateTimeOffset? Timestamp { get; set; }

    // used for cache validation; can be ingored for our use case
    public ETag ETag { get; set; }

    
    public required string Name { get; set; }
    public required string CreationDate { get; set; }
    public required string ImageURL {get; set;}
    public required string Tags {get; set;}     //Is a json serialized List<string>
    public required string Tracks {get; set;}   //Is a json serialized List<Track>; Track member vars: name, trackID, duration, artist, artistID
    public required string DescriptionSummary { get; set; }
    public required string DescriptionDate { get; set; }
}
