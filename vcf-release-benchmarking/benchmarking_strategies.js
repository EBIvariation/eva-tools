db = db.getSiblingDB("eva_testing");

var cursor = db.dbsnpClusteredVariantEntity.aggregate(
	[
	{ 
            "$match" : {
                "asm" : assemblyToUse
            }
    }, 
    { 
      		"$lookup" : {
                "from" : "dbsnpSubmittedVariantEntity",
                "localField" : "accession",
                "foreignField" : "rs",
                "as" : "ssInfo"
            }
    }],    
    { 
        "allowDiskUse" : true
    } 
);

var i = 0;
while (cursor.hasNext()) {
    var result = cursor.next();    
    i += 1;
    if(i%100000 === 0) {
        print("Processed " + i + " documents");
    }
};