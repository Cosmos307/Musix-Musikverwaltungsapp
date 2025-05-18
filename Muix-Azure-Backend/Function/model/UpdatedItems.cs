using Azure;
using Azure.Data.Tables;
using Newtonsoft.Json;

namespace Mountainlabs.Model;

public record UpdatedItems(
    string Type,
    string ID
) {

};