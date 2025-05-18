using Azure;
using Azure.Data.Tables;

namespace Mountainlabs.Model;
public class TrackTableModel : ITableEntity
{
    //type of item: TRACK
    public string PartitionKey { get; set; } = "TRACK";

    //== ID that was auto generated
    public required string RowKey { get; set; } 

    // define last insert / update date
    public DateTimeOffset? Timestamp { get; set; }

    // used for cache validation; can be ingored for our use case
    public ETag ETag { get; set; }

    
    public required string Name { get; set; }           //Always unique in combination with ArtistName
    public required int Duration { get; set; }
    public required string MBID { get; set; }
    public required string ArtistName { get; set; }     //Always unique in combination with Name; the right name for the Artist ID as promised by the trackUpdate, trackCreate & artistUpdate functions
    public required string ArtistID { get; set; }       //Always matches the artist name; promised by the trackUpdate, trackCreate & artistUpdate functions
    public required string ArtistMBID { get; set; }
    public required string Album { get; set; }
    public required string AlbumMBID {get; set;}
    public required int AlbumPos {get; set;}
    public required int Playcount {get; set;}
    public required int Listeners {get; set;}
    public required string ImageURL {get; set;}
    public required string Tags {get; set;}             //Is a json serialized List<string>
    public required string DescriptionSummary { get; set; }
    public required string DescriptionLong { get; set; }
    public required string DescriptionDate { get; set; }
}
