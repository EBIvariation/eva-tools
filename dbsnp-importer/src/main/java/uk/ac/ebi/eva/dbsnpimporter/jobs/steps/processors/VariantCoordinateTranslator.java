package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.dbsnpimporter.models.LocationType;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

class VariantCoordinateTranslator {
    Region getVariantRegion(SubSnpCoreFields subSnpCoreFields) {
        Region variantRegion = subSnpCoreFields.getChromosomeRegion();
        if (variantRegion == null) {
            variantRegion = subSnpCoreFields.getContigRegion();
        }

        // adjust start for insertions
        if (subSnpCoreFields.getLocationType().equals(LocationType.INSERTION)) {
            variantRegion.setStart(variantRegion.getStart() + 1);
            variantRegion.setEnd(variantRegion.getEnd() + subSnpCoreFields.getAlternate().length() - 1);
        }

        return variantRegion != null ? variantRegion : subSnpCoreFields.getContigRegion();
    }

}
