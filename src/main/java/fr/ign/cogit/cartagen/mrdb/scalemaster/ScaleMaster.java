/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.mrdb.scalemaster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ign.cogit.cartagen.mrdb.MRDBPointOfView;
import fr.ign.cogit.cartagen.util.Interval;

/**
 * A ScaleMaster is a set of scale lines (timelines where time is replaced by
 * scale), inspired from the ScaleMaster of Brewer & Buttenfield (2007, 2009).
 * The difference is that, here, the scale lines also contain information on the
 * generalisation processes (with parameters) to apply on the layer in order to
 * make it legible. On the other hand, this class do not contain any display
 * information like symbol widths.
 * 
 * @author GTouya
 * 
 */
public class ScaleMaster {

    /**
     * The name of the Scale Master
     */
    private String name;

    /**
     * The scale lines of {@code this} {@link ScaleMaster}.
     */
    private List<ScaleLine> scaleLines;

    /**
     * The point of view adopted for {@code this} {@link ScaleMaster}
     */
    private MRDBPointOfView pointOfView;

    /**
     * The global range of {@code this} {@link ScaleMaster}: all scale lines use
     * globalRange as the bounds for their own ranges.
     */
    private Interval<Integer> globalRange;

    /**
     * The line that contains multi-theme processes.
     */
    private ScaleMasterMultiLine multiLine;

    private AtomicInteger lineCounter = new AtomicInteger();

    /**
     * Default constructor.
     */
    public ScaleMaster() {
        this.scaleLines = new ArrayList<ScaleLine>();
    }

    public List<ScaleLine> getScaleLines() {
        return scaleLines;
    }

    public void setScaleLines(List<ScaleLine> scaleLines) {
        this.scaleLines = scaleLines;
    }

    public MRDBPointOfView getPointOfView() {
        return pointOfView;
    }

    public void setPointOfView(MRDBPointOfView pointOfView) {
        this.pointOfView = pointOfView;
    }

    public Interval<Integer> getGlobalRange() {
        return globalRange;
    }

    public void setGlobalRange(Interval<Integer> globalRange) {
        this.globalRange = globalRange;
    }

    public ScaleMasterMultiLine getMultiLine() {
        return multiLine;
    }

    public void setMultiLine(ScaleMasterMultiLine multiLine) {
        this.multiLine = multiLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " for " + pointOfView.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((globalRange == null) ? 0 : globalRange.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((pointOfView == null) ? 0 : pointOfView.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScaleMaster other = (ScaleMaster) obj;
        if (globalRange == null) {
            if (other.globalRange != null)
                return false;
        } else if (!globalRange.equals(other.globalRange))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pointOfView == null) {
            if (other.pointOfView != null)
                return false;
        } else if (!pointOfView.equals(other.pointOfView))
            return false;
        return true;
    }

    public int newLineId() {
        return lineCounter.getAndIncrement();
    }

    public ScaleMasterTheme getThemeFromName(String themeName) {
        for (ScaleLine line : scaleLines) {
            if (line.getTheme().getName().equals(themeName))
                return line.getTheme();
        }
        return null;
    }

    /**
     * Retrieves the databases used in the Elements of this ScaleMaster and
     * returns them as a String array.
     * 
     * @return
     */
    public String[] getDatabases() {
        HashSet<String> databases = new HashSet<>();
        for (ScaleLine line : this.getScaleLines()) {
            for (ScaleMasterElement element : line.getAllElements()) {
                databases.add(element.getDbName());
            }
        }

        String[] array = new String[databases.size()];
        Iterator<String> iterator = databases.iterator();
        for (int i = 0; i < databases.size(); i++) {
            array[i] = iterator.next();
        }
        return array;
    }
}
