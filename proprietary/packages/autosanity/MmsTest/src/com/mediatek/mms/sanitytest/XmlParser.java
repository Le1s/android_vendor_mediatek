/**
 * Created on 2010/11/29
 * @author MTK80939
 */

package com.mediatek.mms.sanitytest;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class XmlParser {
    private static final String TAG = "xmlParser";
    private Element mRoot;

    /**
     * Constructor function
     *
     * @param path
     *            String file path to parser
     */
    public XmlParser(String path) {
        Document mDoc;
        try {
            DocumentBuilderFactory mDocBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            mDocBuilderFactory.setValidating(false);
            mDocBuilderFactory.setNamespaceAware(true);
            DocumentBuilder mDocBuilder = mDocBuilderFactory
                    .newDocumentBuilder();
            mDoc = mDocBuilder.parse(new File(path));
            mRoot = mDoc.getDocumentElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * return node of xml file
     *
     * @return Node
     */
    public Node getRootNode() {
        return mRoot;
    }

    /**
     * get node by name in xml file
     *
     * @param ParentNode
     *            Node
     * @param name
     *            String node name to find
     * @return Node
     */
    public Node getNodeByName(Node ParentNode, String name) {
        if (ParentNode == null || name == null) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }

        Node node = ParentNode.getFirstChild();
        while (node != null) {
            if (name.equals(node.getNodeName())) {
                return node;
            }
            node = node.getNextSibling();
        }
        return null;
    }

    /**
     * get node by index
     *
     * @param ParentNode
     *            Node
     * @param nIndex
     *            Integer node index
     * @return Node
     */
    public Node getNodeByIndex(Node ParentNode, int nIndex) {
        NodeList nodeList = ParentNode.getChildNodes();
        int nTemp = nodeList.getLength();
        if (nIndex < 0 || nIndex > nTemp || nTemp <= 0) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }

        return nodeList.item(nIndex);
    }

    /**
     * get node name
     *
     * @param node
     *            Node
     * @return String node name
     */
    public String getNodeName(Node node) {
        if (null == node) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }
        return node.getNodeName();
    }

    /**
     * get node value
     *
     * @param node
     *            Node
     * @return String node value
     */
    public String getNodeValue(Node node) {
        if (null == node) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }

        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE
                    && !subnode.getNodeValue().equals("#text")) {
                return subnode.getNodeValue();
            }
        }
        return null;
    }

    /**
     * get node value and change value to Integer
     *
     * @param node
     *            Node
     * @return Integer node value in Integer
     */
    public int getNodeValueInt(Node node) {
        String str = getNodeValue(node);
        if (str == null) {
            return 0;
        } else {
            return Integer.getInteger(str);
        }
    }

    /**
     * get node value and change value to boolean
     *
     * @param node
     *            Node
     * @return boolean node value in boolean
     */
    public boolean getNodeValueBool(Node node) {
        String str = getNodeValue(node);
        if (str == null) {
            return false;
        } else {
            return Boolean.getBoolean(str);
        }
    }

    /**
     * get Attributes value by attribute name
     *
     * @param node
     *            Node
     * @param AttrName
     *            String Attributes name
     * @return String attribute value
     */
    public String getAttrValue(Node node, String AttrName) {
        if (node == null || AttrName == null) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null)
            return null;

        return attributes.getNamedItem(AttrName).getNodeValue();
    }

    /**
     * get Attributes value by attribute name,and change value to Integer
     *
     * @param node
     *            Node
     * @param AttrName
     *            String
     * @return Integer node value in Integer
     */
    public int getAttrValueInt(Node node, String AttrName) {
        String str = getAttrValue(node, AttrName);
        if (str == null) {
            return 0;
        } else {
            int nTemp = Integer.parseInt(str);
            Log.d("MemTest", "test count is " + nTemp);
            return nTemp;
        }
    }

    /**
     * get Attributes value by attribute name,and change value to boolean
     *
     * @param node
     *            Node
     * @param AttrName
     *            String
     * @return boolean node value in boolean
     */
    public boolean getAttrValueBool(Node node, String AttrName) {
        String str = getAttrValue(node, AttrName);
        if (str == null) {
            return false;
        } else {
            return Boolean.parseBoolean(str);
        }
    }

    /**
     * get node length
     *
     * @param node
     *            Node
     * @return Integer node length
     */
    public int getLength(Node node) {
        if (null == node) {
            Log.d(TAG, "Invalid input parameter");
            return 0;
        }
        NodeList nodeList = node.getChildNodes();
        return nodeList.getLength();
    }

    /**
     * @param ParentNode
     * @param nodeName
     * @return find ParentNode's all child nodes
     */
    public NodeList getNodeList(Node ParentNode, String nodeName) {
        if (ParentNode == null || nodeName == null) {
            Log.d(TAG, "Invalid input parameter");
            return null;
        }
        Element element = (Element) ParentNode;
        return element.getElementsByTagName(nodeName);
    }
}
