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

import embl.ebi.variation.eva.vcfdump.VariantExporterController;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by pagarcia on 03/08/2016.
 */
@RestController
@RequestMapping(value = "/v1/segments")
public class VcfDumperWSServer {

    public Properties evaProperties;

    // TODO: is this constructor necessary?
//    public VcfDumperWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest hsr) {}

    public VcfDumperWSServer() throws IOException {
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET, produces = "application/octet-stream")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getVariantsByRegion(@PathVariable("regionId") String region,
                                                                   @RequestParam(name = "species") String species,
                                                                   @RequestParam(name = "studies", required = false) List<String> studies,
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
        // TODO: HttpServletResponse param is needed?
        // TODO: studies should be mandatory

        // TODO: parse params, right now we are just using region, species and studies
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put(VariantDBAdaptor.REGION, Arrays.asList(region));

        // TODO: just for testing (is the name of the testing database in the development mongo)
        String dbName = "eva_testing";

        // TODO: the controller will compose the name of the file to be downloaded, but this has to be initialized due to the exceptions
        //       reorganize the code, catch the exceptions showing nice errors, letting this variable being initialized inside the catch
        String outputFileName = null;

        // Piped Stream: the content written to the output stream by the VariantExporter will go to the InputStream where the server reads
        // the data from (like if it would be reading a file from the disk)
        PipedInputStream in = new PipedInputStream();
        try {
            OutputStream out = new BufferedOutputStream(new PipedOutputStream(in));

            // build the VariantExporter and run it in a different thread. The Piped stream requires the input and output streams be working
            // in different threads to avoid deadlocks
            VariantExporterController controller = new VariantExporterController(species, dbName, studies, out, evaProperties, queryParameters);
            outputFileName = controller.getOutputFileName();
            Runnable exportTask = () -> {
                controller.run();
            };
            Thread thread = new Thread(exportTask);
            thread.start();
            System.out.println("Secondary thread started");

            // TODO: treat those exceptions
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (StorageManagerException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }

        // set in the output header the name of the file that will be downloaded
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentDispositionFormData("attachment", outputFileName);
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

//        OutputStream
        return ResponseEntity
                .ok()
                // TODO: the next line is not needed because the method itself is annotated with "produces" and also the HttpHeaders
//                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .headers(respHeaders)
                .body(new InputStreamResource(in));
    }
}
