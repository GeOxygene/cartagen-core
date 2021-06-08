/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.core.dataset.shapefile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xerces.dom.DocumentImpl;
import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.ign.cogit.cartagen.core.dataset.CartAGenDB;
import fr.ign.cogit.cartagen.core.dataset.GeneObjImplementation;
import fr.ign.cogit.cartagen.core.dataset.GeographicClass;
import fr.ign.cogit.cartagen.core.dataset.SourceDLM;
import fr.ign.cogit.cartagen.core.dataset.postgis.MappingXMLParser;
import fr.ign.cogit.cartagen.core.dataset.shapefile.ShapeToLayerMapping.ShapeToLayerMatching;
import fr.ign.cogit.cartagen.core.genericschema.AbstractCreationFactory;
import fr.ign.cogit.cartagen.core.genericschema.IGeneObj;
import fr.ign.cogit.cartagen.core.genericschema.energy.IElectricityLine;
import fr.ign.cogit.cartagen.core.genericschema.hydro.IWaterArea;
import fr.ign.cogit.cartagen.core.genericschema.hydro.IWaterLine;
import fr.ign.cogit.cartagen.core.genericschema.network.INetwork;
import fr.ign.cogit.cartagen.core.genericschema.partition.IMask;
import fr.ign.cogit.cartagen.core.genericschema.railway.IRailwayLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.IContourLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.IReliefElementLine;
import fr.ign.cogit.cartagen.core.genericschema.relief.ISpotHeight;
import fr.ign.cogit.cartagen.core.genericschema.road.IRoadLine;
import fr.ign.cogit.cartagen.core.genericschema.urban.IBuilding;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.util.XMLUtil;

public class ShapeFileDB extends CartAGenDB {

    private static Logger logger = Logger
            .getLogger(ShapeFileDB.class.getName());

    private String systemPath;

    private ShapeToLayerMapping mapping;

    private OpenDatasetTask task;

    public ShapeFileDB(File file) throws ParserConfigurationException,
            SAXException, IOException, ClassNotFoundException {
        super(file);
    }

    public ShapeFileDB(String name) {
        this.setName(name);
        this.setPersistentClasses(new HashSet<Class<?>>());
    }

    @Override
    public void openFromXml(File file) throws ParserConfigurationException,
            SAXException, IOException, ClassNotFoundException {

        setClasses(new ArrayList<GeographicClass>());
        // first open the XML document in order to parse it
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        Document doc;
        doc = db.parse(file);
        doc.getDocumentElement().normalize();

        // then read the document to fill the fields
        Element root = (Element) doc.getElementsByTagName("cartagen-dataset")
                .item(0);
        // The DataSet type
        Element typeElem = (Element) root.getElementsByTagName("type").item(0);
        String type = typeElem.getChildNodes().item(0).getNodeValue();
        if (!this.getClass().getName().equals(type)) {
            ShapeFileDB.logger.warning(
                    "The file does not correspond to a ShapeFile Dataset !");
            return;
        }
        // The DataSet name
        Element nameElem = (Element) root.getElementsByTagName("name").item(0);
        this.setName(nameElem.getChildNodes().item(0).getNodeValue());

        // The DataSet symbolisation scale
        Element scaleElem = (Element) root.getElementsByTagName("scale")
                .item(0);
        this.setSymboScale(Integer
                .valueOf(scaleElem.getChildNodes().item(0).getNodeValue()));

        // The DataSet system path
        Element systemPathElem = (Element) root
                .getElementsByTagName("system-path").item(0);
        this.setSystemPath(
                systemPathElem.getChildNodes().item(0).getNodeValue());

        // the source DLM
        Element sourceElem = (Element) root.getElementsByTagName("source-dlm")
                .item(0);
        if (sourceElem != null) {
            SourceDLM source = SourceDLM
                    .valueOf(sourceElem.getChildNodes().item(0).getNodeValue());
            this.setSourceDLM(source);
        }

        // the list of classes
        Element classesElem = (Element) root
                .getElementsByTagName("classes-list").item(0);
        for (int i = 0; i < classesElem.getElementsByTagName("class")
                .getLength(); i++) {
            Element classElem = (Element) classesElem
                    .getElementsByTagName("class").item(i);
            Element pathElem = (Element) classElem.getElementsByTagName("path")
                    .item(0);
            String path = pathElem.getChildNodes().item(0).getNodeValue();
            // Essai Cecile (pour l'instant laissé en commentaires
            // Pour être sûr que les shapefiles sont referencés "en absolu" en
            // utilisant la
            // recupération du répertoire dans lequel s'exécute l'appli (=
            // racine du
            // projet), plutôt que par rapport au repertoire des ressources
            // internes
            // au projet telles que définies dans le classpath
            // On concatène le chemain récupéré dans le xml avec le répertoire
            // courant
            // String path = System.getProperty("user.dir") + "/"
            // + pathElem.getChildNodes().item(0).getNodeValue();
            Element popElem = (Element) classElem
                    .getElementsByTagName("feature-type").item(0);
            String featureType = popElem.getChildNodes().item(0).getNodeValue();
            Class<? extends IGeometry> geometryType = IGeometry.class;
            if (classElem.getElementsByTagName("geometry-type")
                    .getLength() != 0) {
                // TODO
            }
            this.addClass(
                    new ShapeFileClass(this, path, featureType, geometryType));
        }

        // the GeneObjImplementation
        Element implElem = (Element) root
                .getElementsByTagName("geneobj-implementation").item(0);
        Element implNameElem = (Element) implElem
                .getElementsByTagName("implementation-name").item(0);
        String implName = implNameElem.getChildNodes().item(0).getNodeValue();
        Element implPackElem = (Element) implElem
                .getElementsByTagName("implementation-package").item(0);
        String packName = implPackElem.getChildNodes().item(0).getNodeValue();
        Package rootPackage = Package.getPackage(packName);
        Element implClassElem = (Element) implElem
                .getElementsByTagName("implementation-root-class").item(0);
        String className = implClassElem.getChildNodes().item(0).getNodeValue();
        Class<?> rootClass = Class.forName(className);
        Element factClassElem = (Element) implElem
                .getElementsByTagName("implementation-factory").item(0);
        String factClassName = factClassElem.getChildNodes().item(0)
                .getNodeValue();
        Class<?> factClass = Class.forName(factClassName);
        try {
            this.setGeneObjImpl(new GeneObjImplementation(implName, rootPackage,
                    rootClass, (AbstractCreationFactory) factClass
                            .getConstructor().newInstance()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // the persistent classes
        Element persistElem = (Element) root.getElementsByTagName("persistent")
                .item(0);
        this.setPersistent(Boolean
                .valueOf(persistElem.getChildNodes().item(0).getNodeValue()));
        this.setPersistentClasses(new HashSet<Class<?>>());
        Element persistClassesElem = (Element) root
                .getElementsByTagName("persistent-classes").item(0);
        // get the class loader for the geoxygene-cartagen project
        ClassLoader loader = IGeneObj.class.getClassLoader();
        for (int i = 0; i < persistClassesElem
                .getElementsByTagName("persistent-class").getLength(); i++) {
            Element persistClassElem = (Element) persistClassesElem
                    .getElementsByTagName("persistent-class").item(i);
            String className1 = persistClassElem.getChildNodes().item(0)
                    .getNodeValue();
            this.getPersistentClasses()
                    .add(Class.forName(className1, true, loader));
        }
        this.setPersistentClasses(this.getGeneObjImpl()
                .filterClasses(this.getPersistentClasses()));

        // Creates the mapping by reading the xml file
        try {
            Element mappingElem = (Element) root
                    .getElementsByTagName("mapping-file").item(0);
            String mappingXmlFile = mappingElem.getChildNodes().item(0)
                    .getNodeValue();
            MappingXMLParser mappingXMLParser = new MappingXMLParser(
                    new File(mappingXmlFile));
            mapping = mappingXMLParser.parseShapeMapping();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        this.setXmlFile(file);
    }

    /**
     * 
     * {@inheritDoc}
     * <p>
     * 
     * @throws TransformerException
     * 
     */
    @Override
    public void saveToXml(File file) throws IOException, TransformerException {
        Node n = null;
        // ********************************************
        // CREATION DU DOCUMENT XML
        // Document (Xerces implementation only).
        DocumentImpl xmlDoc = new DocumentImpl();
        // Root element.
        Element root = xmlDoc.createElement("cartagen-dataset");
        // The DataSet name
        Element nameElem = xmlDoc.createElement("name");
        n = xmlDoc.createTextNode(this.getName());
        nameElem.appendChild(n);
        root.appendChild(nameElem);
        // The DataSet system path
        Element pathElem = xmlDoc.createElement("system-path");
        n = xmlDoc.createTextNode(this.getSystemPath());
        pathElem.appendChild(n);
        root.appendChild(pathElem);
        // The DataSet type
        Element typeElem = xmlDoc.createElement("type");
        n = xmlDoc.createTextNode(this.getClass().getName());
        typeElem.appendChild(n);
        root.appendChild(typeElem);
        // The DataSet type
        Element datasetTypeElem = xmlDoc.createElement("dataset-type");
        n = xmlDoc.createTextNode(this.getDataSet().getClass().getName());
        datasetTypeElem.appendChild(n);
        root.appendChild(datasetTypeElem);
        // The symbolisation scale
        Element scaleElem = xmlDoc.createElement("scale");
        n = xmlDoc.createTextNode(String.valueOf(this.getSymboScale()));
        scaleElem.appendChild(n);
        root.appendChild(scaleElem);

        // The source DLM
        Element dlmElem = xmlDoc.createElement("source-dlm");
        n = xmlDoc.createTextNode(this.getSourceDLM().name());
        dlmElem.appendChild(n);
        root.appendChild(dlmElem);

        // the list of classes
        Element classesElem = xmlDoc.createElement("classes-list");
        for (GeographicClass c : this.getClasses()) {
            Element classeElem = xmlDoc.createElement("class");
            // the class path
            Element classPathElem = xmlDoc.createElement("path");
            n = xmlDoc.createTextNode(((ShapeFileClass) c).getPath());
            classPathElem.appendChild(n);
            classeElem.appendChild(classPathElem);
            // the population name
            Element popNameElem = xmlDoc.createElement("feature-type");
            n = xmlDoc.createTextNode(c.getFeatureTypeName());
            popNameElem.appendChild(n);
            classeElem.appendChild(popNameElem);
            classesElem.appendChild(classeElem);
        }
        root.appendChild(classesElem);

        // the GeneObj implementation
        Element implElem = xmlDoc.createElement("geneobj-implementation");
        root.appendChild(implElem);
        Element implNameElem = xmlDoc.createElement("implementation-name");
        n = xmlDoc.createTextNode(this.getGeneObjImpl().getName());
        implNameElem.appendChild(n);
        implElem.appendChild(implNameElem);
        Element implPackElem = xmlDoc.createElement("implementation-package");
        n = xmlDoc.createTextNode(
                this.getGeneObjImpl().getRootPackage().getName());
        implPackElem.appendChild(n);
        implElem.appendChild(implPackElem);
        Element implClassElem = xmlDoc
                .createElement("implementation-root-class");
        n = xmlDoc
                .createTextNode(this.getGeneObjImpl().getRootClass().getName());
        implClassElem.appendChild(n);
        implElem.appendChild(implClassElem);
        Element factClassElem = xmlDoc.createElement("implementation-factory");
        n = xmlDoc.createTextNode(this.getGeneObjImpl().getCreationFactory()
                .getClass().getName());
        factClassElem.appendChild(n);
        implElem.appendChild(factClassElem);

        // the persistent classes
        Element persistElem = xmlDoc.createElement("persistent");
        n = xmlDoc.createTextNode(String.valueOf(this.isPersistent()));
        persistElem.appendChild(n);
        root.appendChild(persistElem);
        Element persistClassesElem = xmlDoc.createElement("persistent-classes");
        root.appendChild(persistClassesElem);
        for (Class<?> classObj : this.getPersistentClasses()) {
            Element persistClassElem = xmlDoc.createElement("persistent-class");
            n = xmlDoc.createTextNode(classObj.getName());
            persistClassElem.appendChild(n);
            persistClassesElem.appendChild(persistClassElem);
        }

        // ECRITURE DU FICHIER
        xmlDoc.appendChild(root);
        this.print();
        XMLUtil.writeDocumentToXml(xmlDoc, file);
    }

    /**
     * Add a new shapfile to the dataset
     * 
     * @param shape
     * @param populationName
     *            the name of the population of objects created from the
     *            Shapefile and stored in GeneralisationDataset
     */
    public void addShapeFile(String path, String populationName,
            Class<? extends IGeometry> geometryType) {
        this.addClass(
                new ShapeFileClass(this, path, populationName, geometryType));
    }

    @Override
    public void populateDataset(int scale) {
        for (GeographicClass layer : this.getClasses()) {
            System.out.println("\n loading layer: " + layer.getName());
            this.load(layer, scale);
        }

        // now build the dataset networks from the loaded data
        INetwork roadNet = this.getDataSet().getRoadNetwork();
        for (IRoadLine road : this.getDataSet().getRoads()) {
            roadNet.addSection(road);
        }
        INetwork railNet = this.getDataSet().getRailwayNetwork();
        for (IRailwayLine rail : this.getDataSet().getRailwayLines()) {
            railNet.addSection(rail);
        }
        INetwork waterNet = this.getDataSet().getHydroNetwork();
        for (IWaterLine water : this.getDataSet().getWaterLines()) {
            waterNet.addSection(water);
        }

        /*
         * this.task = new OpenDatasetTask(scale, this); this.task.execute();
         * while (!this.task.exit) { System.out.println("on passe par là"); try
         * { Thread.sleep(10); } catch (InterruptedException e) {
         * e.printStackTrace(); } }
         */
    }

    @Override
    protected void load(GeographicClass geoClass, int scale) {
        ShapeFileClass shape = (ShapeFileClass) geoClass;
        String filename = new File(shape.getFileName()).getName();
        filename = filename.substring(0, filename.length() - 4);
        ShapeToLayerMatching matching = mapping
                .getMatchingFromShapefile(filename);
        try {
            ShapeFileLoader.genericShapefileLoader(shape.getPath(),
                    this.getDataSet(), matching.getShapeLayer(),
                    matching.getCreationMethod(),
                    this.getGeneObjImpl().getCreationFactory(),
                    matching.getListAttr(), false);
        } catch (NoSuchFieldException | SecurityException
                | InvocationTargetException e) {
            e.printStackTrace();
        }

        /*
         * try { if
         * (shape.getFeatureTypeName().equals(IBuilding.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadBuildingsFromSHP(shape.getPath(),
         * this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(IRoadLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadRoadLinesFromSHP(shape.getPath(),
         * this.getSourceDLM(), this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(IWaterLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadWaterLinesFromSHP(shape.getPath(),
         * this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(IWaterArea.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadWaterAreasFromSHP(shape.getPath(),
         * this.getDataSet()); } if (shape.getFeatureTypeName()
         * .equals(IRailwayLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadRailwayLineFromSHP(shape.getPath(), null,
         * this.getDataSet()); } if (shape.getFeatureTypeName()
         * .equals(IElectricityLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadElectricityLinesFromSHP(shape.getPath(),
         * this.getDataSet()); } if (shape.getFeatureTypeName()
         * .equals(IContourLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadContourLinesFromSHP(shape.getPath(),
         * this.getDataSet()); } if (shape.getFeatureTypeName()
         * .equals(IReliefElementLine.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadReliefElementLinesFromSHP(shape.getPath(),
         * this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(ISpotHeight.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadSpotHeightsFromSHP(shape.getPath(),
         * this.getDataSet()); } if (shape.getFeatureTypeName()
         * .equals(ISportsField.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadSportsFieldsBDTFromSHP(shape.getPath(),
         * this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(ICemetery.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadCemeteriesBDTFromSHP(shape.getPath(),
         * this.getDataSet()); }
         * 
         * if (shape.getFeatureTypeName().equals(IUrbanBlock.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadBlocksFromSHP(shape.getPath(),
         * this.getDataSet()); } if
         * (shape.getFeatureTypeName().equals(ITown.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadTownsFromSHP(shape.getPath(), this.getDataSet());
         * }
         * 
         * if (shape.getFeatureTypeName().equals(ILabelPoint.FEAT_TYPE_NAME)) {
         * LabelCategory category = LabelCategory.OTHER; if
         * (shape.getName().equalsIgnoreCase("LIEU_DIT_HABITE")) category =
         * LabelCategory.LIVING_PLACE; if
         * (shape.getName().equalsIgnoreCase("LIEU_DIT_NON_HABITE")) category =
         * LabelCategory.PLACE; if (shape.getName().equalsIgnoreCase("ORONYME"))
         * category = LabelCategory.RELIEF; if
         * (shape.getName().equalsIgnoreCase("HYDRONYME")) category =
         * LabelCategory.WATER; if
         * (shape.getName().equalsIgnoreCase("TOPONYME_COMMUNICATION")) category
         * = LabelCategory.COMMUNICATION;
         * ShapeFileLoader.loadLabelPointsFromSHP(shape.getPath(),
         * this.getDataSet(), category); } if
         * (shape.getFeatureTypeName().equals(IMask.FEAT_TYPE_NAME)) {
         * ShapeFileLoader.loadMaskFromSHP(shape.getPath(), this.getDataSet());
         * } if (shape.getFeatureTypeName()
         * .equals(ISimpleLandUseArea.FEAT_TYPE_NAME)) { int type = 0; if
         * (shape.getName().equalsIgnoreCase("ZONE_VEGETATION")) { type = 1; }
         * else if (shape.getName().equalsIgnoreCase("ZONE_ARBOREE")) { type =
         * 1; } else if (shape.getName().equalsIgnoreCase("ZONE_ACTIVITE")) {
         * type = 2; } ShapeFileLoader.loadLandUseAreasFromSHP(shape.getPath(),
         * 1.0, type, this.getDataSet()); } // add the unique Id in the
         * ShapeFile // this.addCartagenId();
         */
    }

    /**
     * 
     * {@inheritDoc}
     * <p>
     * 
     */
    @Override
    public void overwrite(GeographicClass geoClass) {
        ShapeFileClass shape = (ShapeFileClass) geoClass;

        try {
            if (shape.getFeatureTypeName().equals(IBuilding.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteBuildingsFromSHP(shape.getPath(),
                        this.getDataSet());
            }
            if (shape.getFeatureTypeName().equals(IRoadLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteRoadLinesFromSHP(shape.getPath(), 2.0,
                        this.getSourceDLM(), this.getDataSet());
            }
            if (shape.getFeatureTypeName().equals(IWaterLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteWaterLinesFromSHP(shape.getPath(), 2.0,
                        this.getDataSet());
            }
            if (shape.getFeatureTypeName().equals(IWaterArea.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteWaterAreasFromSHP(shape.getPath(), 2.0,
                        this.getDataSet());
            }
            if (shape.getFeatureTypeName()
                    .equals(IRailwayLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteRailwayLineFromSHP(shape.getPath(),
                        2.0, this.getDataSet());
            }
            if (shape.getFeatureTypeName()
                    .equals(IElectricityLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteElectricityLinesFromSHP(
                        shape.getPath(), 2.0, this.getDataSet());
            }
            if (shape.getFeatureTypeName()
                    .equals(IContourLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteContourLinesFromSHP(shape.getPath(),
                        2.0, this.getDataSet());
            }
            if (shape.getFeatureTypeName()
                    .equals(IReliefElementLine.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteReliefElementLinesFromSHP(
                        shape.getPath(), 2.0, this.getDataSet());
            }
            if (shape.getFeatureTypeName().equals(ISpotHeight.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteSpotHeightsFromSHP(shape.getPath(),
                        this.getDataSet());
            }
            if (shape.getFeatureTypeName().equals(IMask.FEAT_TYPE_NAME)) {
                ShapeFileLoader.overwriteMaskFromSHP(shape.getPath(),
                        this.getDataSet());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    public String getSystemPath() {
        return this.systemPath;
    }

    public void setTask(OpenDatasetTask task) {
        this.task = task;
    }

    public OpenDatasetTask getTask() {
        return this.task;
    }

    public class OpenDatasetTask extends SwingWorker<Void, Void> {

        private int scale;
        public boolean exit = false;

        @Override
        protected Void doInBackground() throws Exception {
            // loop on the classes to import them into the generalisation
            // dataset
            int step = Math.round(100 / ShapeFileDB.this.getClasses().size());
            int value = 0;
            System.out
                    .println("loop on the geoclasses: " + getClasses().size());
            System.out.println(getClasses());
            for (GeographicClass shapefile : getClasses()) {
                // test if the shapefile correspond to a persistent class
                System.out.println(shapefile);
                boolean persistent = false;
                for (Class<?> classObj : ShapeFileDB.this
                        .getPersistentClasses()) {
                    if (!IGeneObj.class.isAssignableFrom(classObj)) {
                        continue;
                    }
                    Field field = classObj.getField("FEAT_TYPE_NAME");
                    String featType = (String) field.get(null);
                    if (shapefile.getFeatureTypeName().equals(featType)) {
                        persistent = true;
                    }
                }
                if (persistent) {
                    continue;
                }
                System.out.println("load shapefile");
                ShapeFileDB.this.load(shapefile, this.scale);
                System.out.println("test");
                value += step;
                this.setProgress(value);
                System.out.println("test2");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.exit = true;
                }
            }

            // trigger the enrichments
            if (!ShapeFileDB.this.isPersistent()) {
                ShapeFileDB.this.triggerEnrichments();
            }
            // finally load persistent classes
            try {
                // now build the dataset networks from the loaded data
                INetwork roadNet = ShapeFileDB.this.getDataSet()
                        .getRoadNetwork();
                for (IRoadLine road : ShapeFileDB.this.getDataSet()
                        .getRoads()) {
                    roadNet.addSection(road);
                }
                INetwork railNet = ShapeFileDB.this.getDataSet()
                        .getRailwayNetwork();
                for (IRailwayLine rail : ShapeFileDB.this.getDataSet()
                        .getRailwayLines()) {
                    railNet.addSection(rail);
                }
                INetwork waterNet = ShapeFileDB.this.getDataSet()
                        .getHydroNetwork();
                for (IWaterLine water : ShapeFileDB.this.getDataSet()
                        .getWaterLines()) {
                    waterNet.addSection(water);
                }
                // CartAGenDoc.getInstance().getCurrentDataset().addFeatureAttribute();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (HibernateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.exit = true;
            }
            return null;
        }

        OpenDatasetTask(int scale, ShapeFileDB dataset) {
            super();
            this.scale = scale;
            dataset.task = this;
        }
    }

    @Override
    public void addCartagenId() {
        for (GeographicClass shape : this.getClasses()) {
            shape.addCartAGenId();
        }
    }

    public void print() {
        System.out.println("ShapeFileDB: " + this.getName());
        System.out.println("classes: " + this.getClasses());
        System.out.println("path: " + this.systemPath);
    }

    @Override
    protected void triggerEnrichments() {
        // TODO Auto-generated method stub

    }
}
