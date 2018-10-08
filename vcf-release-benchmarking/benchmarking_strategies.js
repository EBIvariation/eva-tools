db = db.getSiblingDB("eva_testing");
var strategyLookupMap = {"3.1": multipleSSLookupPerRS, "4": MongoLookup};
strategyLookupMap[strategyToUse](db);

function multipleSSLookupPerRS (db) {
    cursor = db.dbsnpClusteredVariantEntity.find({asm:assemblyToUse})
    if (! cursor.hasNext() ) {
        print("no element found");
    } else {
        while ( cursor.hasNext() ){
            //print("\nelement: ");
            rs = cursor.next();
            //printjson(rs);
            ss_cursor = db.dbsnpSubmittedVariantEntity.find({rs:rs.accession});
            group_by_position = {};
            positions = [];
            while (ss_cursor.hasNext()) {
                ss = ss_cursor.next();
                position = '' +ss.contig + ":" + ss.start; // need a string to put position as a key
                if (group_by_position[position] == undefined) {
                    group_by_position[position] = [];
                    positions.push(position);
                }
                group_by_position[position].push(ss);
            }
            positions.forEach(function(pos) {
                        line = 'subsnps of rs' + rs.accession + " at " + pos + "= ";
                        group_by_position[pos].forEach(function(ss) {
                                line += ss.study + ":" + ss.contig + ":" + ss.start + ":" + ss.ref + ":" + ss.alt + " ";
                            });
                        //print(line);
                    });
        }
    }
}

function MongoLookup (db) {
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
}