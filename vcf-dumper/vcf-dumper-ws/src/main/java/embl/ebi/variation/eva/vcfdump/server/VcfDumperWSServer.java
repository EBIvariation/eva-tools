/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package embl.ebi.variation.eva.vcfdump.server;

import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created by pagarcia on 03/08/2016.
 */
@RestController
@RequestMapping(value = "/v1/segments")
public class VcfDumperWSServer {
    // TODO: is this necesary?
//    public VcfDumperWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {}
    public VcfDumperWSServer() {

    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getVariantsByRegion(@PathVariable("regionId") String regionId,
                                    @RequestParam(name = "species") String species,
                                    @RequestParam(name = "studies", required = false) String studies,
                                    @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
                                    @RequestParam(name = "maf", defaultValue = "") String maf,
                                    @RequestParam(name = "polyphen", defaultValue = "") String polyphenScore,
                                    @RequestParam(name = "sift", defaultValue = "") String siftScore,
                                    @RequestParam(name = "ref", defaultValue = "") String reference,
                                    @RequestParam(name = "alt", defaultValue = "") String alternate,
                                    @RequestParam(name = "miss_alleles", defaultValue = "") String missingAlleles,
                                    @RequestParam(name = "miss_gts", defaultValue = "") String missingGenotypes,
                                    @RequestParam(name = "histogram", defaultValue = "false") boolean histogram,
                                    @RequestParam(name = "histogram_interval", defaultValue = "-1") int interval,
                                    @RequestParam(name = "merge", defaultValue = "false") boolean merge,
                                    HttpServletResponse response) {
        // TODO: throws IllegalOpenCGACredentialsException, IOException {?
        return "Asked for region " + regionId + " of species " + species;
    }
}
