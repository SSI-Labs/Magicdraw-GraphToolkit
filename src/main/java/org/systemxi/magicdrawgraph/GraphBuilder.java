package org.systemxi.magicdrawgraph;

import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

import java.awt.*;
import java.util.List;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class GraphBuilder
{
    private static final boolean DEBUG = false;

    private HashMap<String, HashMap<String, String>> nodeMap = new java.util.HashMap<String, HashMap<String, String>>();
    private HashSet<Element> addNodeList = new java.util.HashSet();
    private HashMap<String, HashMap> edgeMap = new java.util.HashMap<String, HashMap>();
    private HashMap<String, String> mapL2SID = new java.util.HashMap<String, String>();
    private long nextID = 1;

    public void GenerateGML() throws Exception {


        Application app = Application.getInstance();
        //log = app.getGUILog();
        Project proj = app.getProject();

        long starttime = System.currentTimeMillis();
        log("Starting GML export...");

        Browser browser = proj.getBrowser();
        Node[] selectedNodes = browser.getActiveTree().getSelectedNodes();
        if (selectedNodes != null) {
            //userObject = selectedNode.getUserObject();
            for (Node sn : selectedNodes) {
                Object userObject = sn.getUserObject();
                if (userObject instanceof Element) {
                   // Finder.ByScopeFinder bsf = Finder.byScope();


                    Collection<Element> allElements =  Finder.byScope().find((Element)userObject, true);
                    for(Element element : allElements) {
                        debug("Element: " + element.getHumanType());
                        followNetwork(element);
                    }

                    for(Element element : addNodeList) {
                        debug("Additional Element: " + element.getHumanType());
                        addNode(element);
                    }
                }
            }
        }


        // Dialog...
        FileDialog dialog = new java.awt.FileDialog((Frame)null, "Select filename to save GML", FileDialog.SAVE);
        dialog.setMultipleMode(false);
        dialog.show();
        String filename = dialog.getFile();
        if(filename!=null) {
            filename = dialog.getDirectory() + "\\" + filename;
        }
        if(filename!=null) {


            String gml = toGML();

            FileOutputStream out = new FileOutputStream(filename);
            out.write(gml.getBytes());
            out.close();
            log("Finished GML export in " + (System.currentTimeMillis() - starttime)/1000 + " seconds");
        } else {

            log("Cancelled");
        }
    }



    private void log(String msg) {
        Application.getInstance().getGUILog().log(msg);
    }

    private void debug(String msg) {
        if(DEBUG==true) { log(msg); }
    }

    private String clean(String in) {
        String s = in;
        //s = s.substring(0, Math.min(s.length(), 100));
        s = s.replace("\"", "&quot");
        //s = s.replace("/", "");
        //s = s.replace("&", "");

        byte[] b = s.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        return new String(b);

    }

    private String getShortID(String longid) {
        if(!mapL2SID.containsKey(longid)) {
            mapL2SID.put(longid, nextID+"");
            nextID++;
        }
        return mapL2SID.get(longid);
    }


    private boolean nodeExists(Element element) {
        return  nodeMap.containsKey(getShortID(element));

    }

    private void addEdge(Element source, Element target, String type)     {
        addNodeList.add(source);
        addNodeList.add(target);
        String id = getShortID(source.getID() + target.getID() + type);
        HashMap attrs = new java.util.HashMap();

        attrs.put("Source", getShortID (source.getID()));
        attrs.put("Target", getShortID(target.getID()));
        attrs.put("Type", type);

        edgeMap.put(id, attrs);
    }

    private void addNode(Element element)     {


        HashMap attrs = new java.util.HashMap();
        if(element instanceof NamedElement) {
            attrs.put("Name", ((NamedElement) element).getName());
            attrs.put("QualifiedName", ((NamedElement)element).getQualifiedName());
        }
        attrs.put("Type", element.getHumanType());

        nodeMap.put(getShortID(element), attrs);

    }

    private String toGML() {
        StringBuffer result = new StringBuffer();
        result.append("graph [\n");

        for(String key:nodeMap.keySet()) {

            StringBuffer sb = new StringBuffer();
            sb.append( "\tnode [ ");
            sb.append( "id " + key + " ");

            HashMap<String, String> attrs = nodeMap.get(key);
            for (String attr_key:attrs.keySet()) {
                String val = attrs.get(attr_key);
                if(val!=null) {
                    sb.append(attr_key +  " \"" + clean(attrs.get(attr_key)) + "\" ");
                }
            }
            //sb.append("name" +  " \"" + nodeMap.get(key).get("Name") + "\" ");
            //sb.append("type" +  " \"" + nodeMap.get(key).get("Type") + "\" ");
            //sb.append("qualifiedName" +  " \"" + nodeMap.get(key).get("QualifiedName") + "\"");
            sb.append ("] \n");
            result.append(sb);
        }
        for(String key:edgeMap.keySet()) {

            StringBuffer sb = new StringBuffer();
            sb.append( "\tedge [ ");
            sb.append( "source " + edgeMap.get(key).get("Source") + " ");
            sb.append( "target " + edgeMap.get(key).get("Target") + " ");
            sb.append( "label \"" + edgeMap.get(key).get("Type") + "\" ");
            sb.append ("] \n");
            result.append(sb);
        }
        result.append("]\n");

        return result.toString();
    }
    private String getShortID(Element element) {
        return getShortID(element.getID());

    }

    private static Element getFirstElementInCollection(Collection<Element> elements) { return (Element) elements.toArray()[0]; }

    private void followNetwork(Element element)     {

        String humanType =    element.getHumanType();
        if ((humanType.equals("Diagram") || humanType.equals("Customization") || humanType.equals("Element Value") || humanType.equals("Slot") || humanType.equals("Instance Specification") || humanType.equals("Instance Value") || humanType.startsWith("Literal") )) {
            // ignore
        } else if (element instanceof DirectedRelationship) {
            DirectedRelationship dr = (DirectedRelationship) element;


            addEdge(getFirstElementInCollection(dr.getSource()), getFirstElementInCollection(dr.getTarget()), dr.getHumanType());
        } else if (element instanceof NamedElement) {

            addNodeList.add(element);

            if(element.getOwner()!=null) {
                addEdge(element, element.getOwner(), "Owner");
            }

            List<Slot> slots = com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper.collectOwnedSlots(element);

            for(Slot slot : slots) {

                List<ValueSpecification> slotVal = slot.getValue();
                String featName =   slot.getDefiningFeature().getFeaturingClassifier().getName() + "." + slot.getDefiningFeature().getName();

                for(ValueSpecification sv : slotVal) {
                    if (sv instanceof ElementValue ) {
                        Element er = ((ElementValue) sv).getElement();
                        addEdge(element, er, featName);
                    }
                }

            }

        }
    }

}


