/*
 * SListModel
 */

package mixedmodeaca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;

/**
 *
 * @author Administrator
 */
class SListModel extends AbstractListModel {
    //Set model;
    List model;
    public SListModel() {
        //model = new TreeSet();
        model = new ArrayList();
    }
    public int getSize() {
        return model.size();
    }
    public Object getElementAt (int index) {
        return model.toArray()[index];
    }
    public void add(Object element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }
    public void addAll (Object elements[]) {
        //System.out.println("From SListModel: " + elements[0]);
        Collection c  = Arrays.asList(elements);
        model.addAll(c);
        fireContentsChanged(this, 0, getSize());
    }
    public void clear() {
        model.clear();
        fireContentsChanged(this, 0, getSize());
    }
    public boolean contains(Object element) {
        return model.contains(element);
    }
    public Object firstElement() {
        return model.toArray()[0];
    }
    public Iterator iterator() {
        return model.iterator();
    }
    public Object lastElement() {
        return model.toArray()[model.size()-1];
    }
    public boolean removeElement(Object element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }
}
