package de.lmu.ifi.dbs.algorithm.result;

import de.lmu.ifi.dbs.data.RealVector;
import de.lmu.ifi.dbs.database.AssociationID;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.distance.Distance;
import de.lmu.ifi.dbs.normalization.NonNumericFeaturesException;
import de.lmu.ifi.dbs.normalization.Normalization;
import de.lmu.ifi.dbs.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a result of a clustering-algorithm that computes hierarchical axes parallel
 * clusters and a preference vectors for each cluster from a cluster order.
 *
 * @author Elke Achtert (<a
 *         href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public class HierarchicalAxesParallelClusters<O extends RealVector, D extends Distance<D>> extends AbstractResult<O> {
  /**
   * Indicating the preference vector of a cluster in the string representation.
   */
  public static String PREFERENCE_VECTOR = "preference vector: ";

  /**
   * Indicating the children of a cluster in the string representation.
   */
  public static String CHILDREN = "children: ";

  /**
   * Indicating the parents of a cluster in the string representation.
   */
  public static String PARENTS = "parents: ";

  /**
   * Indicating the level of a cluster in the string representation.
   */
  public static String LEVEL = "level: ";

  /**
   * Indicating the index within the level of a cluster in the string representation.
   */
  public static String LEVEL_INDEX = "level index: ";

  /**
   * The root cluster.
   */
  private HierarchicalAxesParallelCluster rootCluster;

  /**
   * The cluster order.
   */
  private ClusterOrder<O, D> clusterOrder;

  /**
   * Provides a result of a clustering-algorithm that computes hierarchical axes parallel
   * clusters and a preference vectors for each cluster from a cluster order.
   *
   * @param rootCluster  the root cluster
   * @param db           the database containing the objects of clusters
   * @param clusterOrder the cluster order
   */
  public HierarchicalAxesParallelClusters(HierarchicalAxesParallelCluster rootCluster,
                                          ClusterOrder<O, D> clusterOrder,
                                          Database<O> db) {
    super(db);
    this.rootCluster = rootCluster;
    this.clusterOrder = clusterOrder;
  }

  /**
   * Writes the cluster order to the given stream.
   *
   * @param outStream     the stream to write to
   * @param normalization Normalization to restore original values according to, if this action is supported
   *                      - may remain null.
   * @param settings      the settings to be written into the header, if this parameter is <code>null</code>,
   *                      no header will be written
   * @throws de.lmu.ifi.dbs.utilities.UnableToComplyException
   *          if any feature vector is not compatible with values initialized during normalization
   */
  public void output(PrintStream outStream, Normalization<O> normalization, List<AttributeSettings> settings) throws UnableToComplyException {
    clusterOrder.output(outStream, normalization, settings);
  }

  /**
   * @see de.lmu.ifi.dbs.algorithm.result.Result#output(java.io.File, de.lmu.ifi.dbs.normalization.Normalization, java.util.List)
   */
  public void output(File dir,
                     Normalization<O> normalization,
                     List<AttributeSettings> settings) throws UnableToComplyException {

    dir.mkdirs();
    clusterOrder.output(new File(dir.getAbsolutePath() + File.separator + "clusterOrder"),
                        normalization,
                        settings);

    try {
      File outFile = new File(dir.getAbsolutePath() + File.separator + rootCluster.toString());
      PrintStream outStream = new PrintStream(new FileOutputStream(outFile));
      write(rootCluster, dir, outStream, normalization, settings, new HashMap<HierarchicalAxesParallelCluster, Boolean>());
    }
    catch (NonNumericFeaturesException e) {
      throw new UnableToComplyException(e);
    }
    catch (FileNotFoundException e) {
      throw new UnableToComplyException(e);
    }
  }

  /**
   * Writes a cluster to the designated print stream.
   *
   * @param cluster       the cluster to be written
   * @param dir           the directory where to write
   * @param normalization a Normalization to restore original values for output - may
   *                      remain null
   * @param settings      the settings to be written into the header
   * @throws de.lmu.ifi.dbs.normalization.NonNumericFeaturesException
   *          if feature vector is not compatible with values initialized
   *          during normalization
   */
  private void write(HierarchicalAxesParallelCluster cluster,
                     File dir,
                     PrintStream out,
                     Normalization<O> normalization,
                     List<AttributeSettings> settings,
                     Map<HierarchicalAxesParallelCluster, Boolean> written) throws NonNumericFeaturesException, FileNotFoundException {

    BitSet preferenceVector = cluster.getPreferenceVector();
    writeHeader(out, settings, null);
    out.println("### " + PREFERENCE_VECTOR + Util.format(getDatabase().dimensionality(), preferenceVector));
    out.print("### " + CHILDREN);
    for (int i = 0; i < cluster.getChildren().size(); i++) {
      HierarchicalAxesParallelCluster c = cluster.getChildren().get(i);
      out.print(c);
      if (i < cluster.getChildren().size() - 1)
        out.print(":");

    }
    out.println();
    out.print("### " + PARENTS);
    for (int i = 0; i < cluster.getParents().size(); i++) {
      HierarchicalAxesParallelCluster c = cluster.getParents().get(i);
      out.print(c);
      if (i < cluster.getParents().size() - 1)
        out.print(":");
    }
    out.println();
    out.println("### " + LEVEL + cluster.getLevel());
    out.println("### " + LEVEL_INDEX + cluster.getLevelIndex());
    out.println("################################################################################");

    List<Integer> ids = cluster.getIDs();
    for (Integer id : ids) {
      O v = db.get(id);
      if (normalization != null) {
        v = normalization.restore(v);
      }
      out.println(v.toString()
                  + SEPARATOR
                  + db.getAssociation(AssociationID.LABEL, id));
    }
    out.flush();
    written.put(cluster, true);

    // write the children
    List<HierarchicalAxesParallelCluster> children = cluster.getChildren();
    for (HierarchicalAxesParallelCluster child : children) {
      Boolean done = written.get(child);
      if (done != null && done) continue;


      File outFile = new File(dir.getAbsolutePath() + File.separator + child.toString());
      outFile.getParentFile().mkdirs();
      PrintStream outStream = new PrintStream(new FileOutputStream(outFile, true), true);
      write(child, dir, outStream, normalization, settings, written);
    }
  }

  /**
   * Returns the root cluster.
   *
   * @return the root cluster
   */
  public HierarchicalAxesParallelCluster getRootCluster() {
    return rootCluster;
  }

  /**
   * Returns the cluster order.
   *
   * @return the cluster order
   */
  public ClusterOrder<O, D> getClusterOrder() {
    return clusterOrder;
  }
}
