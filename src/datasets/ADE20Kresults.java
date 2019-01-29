/**
 * Author: XauthorX
 * 18/12/17
 */
package datasets;

import matrix.Matrix2D;
import matrix.MatrixReader;
import scenery.ASceneryEval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ADE20Kresults extends ADE20K{
    private String predictionsPath;

    public ADE20Kresults(String rootPath) throws IOException {
        super(Paths.get(rootPath,"ADEChallengeData2016").toString());
        this.predictionsPath = Paths.get(rootPath,"predictions").toString()+ File.separator;
    }

    /**
     * Read annotated scenery, from sample name (e.g. ADE20K.getTestSet() or ADE20Kresults.getTestSet()).
     * The sample must belong to the test set: ASceneryEval contains the class predictions and ground truth.
     */
    public ASceneryEval readASceneryEval(String sampleName) throws IOException {
        Matrix2D predictions = MatrixReader.readMatrix2D(new File(predictionsPath+sampleName+".png"));
        Matrix2D groundTruth = MatrixReader.readMatrix2D(getTestAnnotationFile(sampleName));
        return new ASceneryEval(predictions, groundTruth);
    }

    /**
     * Get test set file names
     * @param onlyFilesWithPredictions if this value is true, it returns ONLY the names of the samples
     *                                 which have been labeled by the neural network (i.e. the corresponding labeling file is present
     *                                 in the "predictions" folder)
     * @param limit max number of files; -1 if unbounded
     * @return the sample names
     */
    public List<String> getTestSet(boolean onlyFilesWithPredictions, int limit) {
        if (!onlyFilesWithPredictions)
            return getTestSet(limit);//older version

        //New version: with only existing predictions
        List<String> res = new LinkedList<>();
        File[] files = new File(predictionsPath).listFiles();
        if (files == null) return null;
        //Get files, without extensions
        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                res.add(file.getName().split("\\.")[0]);
                count++;
                if (count>=limit && limit!=-1) break;
            }
        }
        return res;
    }
}
