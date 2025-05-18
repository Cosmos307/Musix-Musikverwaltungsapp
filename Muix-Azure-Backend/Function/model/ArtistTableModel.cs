using Azure;
using Azure.Data.Tables;

namespace Mountainlabs.Model;
public class ArtistTableModel : ITableEntity
{
    //type of item: ARTIST
    public string PartitionKey { get; set; } = "ARTIST";

    //artist unique ID
    public required string RowKey { get; set; }

    // define last insert / update date
    public DateTimeOffset? Timestamp { get; set; }

    // used for cache validation; can be ingored for our use case
    public ETag ETag { get; set; }
    
    //Is unique (create & update respect that)
    public required string Name { get; set; }

    public required string MBID { get; set; }
    public required string DescriptionSummary { get; set; }
    public required string DescriptionLong { get; set; }
    public required string DescriptionDate { get; set; }
    public required string ImageURL {get; set;}
    public required int Listeners {get; set;}
    public required string Tags {get; set;}         //Is a json serialized List<string>
}
