using Azure;
using Azure.Data.Tables;
using Newtonsoft.Json;

namespace Mountainlabs.Model;

public record ErrorModelCascadingUpdate(
    string ArtistID,
    List<UpdatedItems> FinishedUpdatedItems
) {

};