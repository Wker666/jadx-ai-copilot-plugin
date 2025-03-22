package jadx.plugins.ai.ui;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.plugins.JadxPluginContext;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.codearea.AbstractCodeArea;
import jadx.gui.ui.panel.ContentPanel;
import jadx.plugins.ai.JadxAiViewAction;
import jadx.plugins.ai.ai.LangchainOpenAiChatModel;
import jadx.plugins.ai.config.PromptConfigManager;
import jadx.plugins.ai.config.PromptType;
import jadx.plugins.ai.module.GraphStructure;
import jadx.plugins.ai.module.QAMap;
import jadx.plugins.ai.utils.PromptGenerate;
import jadx.plugins.ai.utils.Helper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class GraphPanel {
	private mxGraph jGraphXGraph;
	private mxGraphComponent graphComponent;

	private JPanel mainPanel;

	private Map<JavaNode, Object> vertexMap;
	private Map<Object, JavaNode> vertexDataMap;
	private final JadxPluginContext context;
	private GraphStructure graphStructure;
	private PromptConfigManager promptConfigManager;

	private MarkdownProcessor markdownProcessor;

	private List<QAMap> QueAnsHistory = new ArrayList<>();

	private StatusBar statusBar;

	Map<String,JavaNode> allClassNodeName = new HashMap<>();
	Map<String,JavaNode> allMethodNodeName = new HashMap<>();
	Map<String,JavaNode> allFieldNodeName = new HashMap<>();


	private static final Map<ICodeAnnotation.AnnType,String> AnnotationStyles = new HashMap<>();

	static{
		AnnotationStyles.put(ICodeAnnotation.AnnType.METHOD,"shape=ellipse;fillColor=lightsalmon");
		AnnotationStyles.put(ICodeAnnotation.AnnType.CLASS,"shape=rectangle;fillColor=lightgreen");
		AnnotationStyles.put(ICodeAnnotation.AnnType.FIELD,"shape=cloud;fillColor=lightcoral");
	}


	public GraphPanel(JadxPluginContext context, GraphStructure graphStructure, PromptConfigManager promptConfigManager,
					  MarkdownProcessor markdownProcessor) {
		this.context = context;
		this.graphStructure = graphStructure;
		this.promptConfigManager = promptConfigManager;
		this.markdownProcessor = markdownProcessor;
		vertexMap = new HashMap<>();
		vertexDataMap = new HashMap<>();

		context.getDecompiler().getClasses()
				.forEach(aClass -> allClassNodeName.put(aClass.getFullName(), aClass));
		context.getDecompiler().getClasses().stream()
				.flatMap(aClass -> aClass.getMethods().stream())
				.forEach(method -> allMethodNodeName.put(method.getFullName(), method));
		context.getDecompiler().getClasses().stream()
				.flatMap(aClass -> aClass.getFields().stream())
				.forEach(Field -> allFieldNodeName.put(Field.getFullName(), Field));

		initializeGraph();
	}


	private void initializeGraph() {
		jGraphXGraph = new mxGraph();
		graphComponent = new mxGraphComponent(jGraphXGraph);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);

		// Disable the connection and editing capabilities of graphical components
		graphComponent.setConnectable(false);
		graphComponent.getGraph().setCellsEditable(false);
		graphComponent.setDragEnabled(true); // Disable node dragging, only allow canvas dragging
//		graphComponent.getGraph().setCellsMovable(true);
//		graphComponent.getGraph().setCellsResizable(false);
//		graphComponent.getGraph().setCellsBendable(false);
//		graphComponent.getGraph().setCellsDeletable(false);

		// Enable mouse selection
		new mxRubberband(graphComponent);
		refreshGraph();

		statusBar = new StatusBar();
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(graphComponent, BorderLayout.CENTER);
		mainPanel.add(statusBar, BorderLayout.SOUTH);


		JPopupMenu popupMenu = new JPopupMenu();

		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				if (cell == null || !jGraphXGraph.getModel().isVertex(cell)){
					return ;
				}
				JavaNode node = vertexDataMap.get(cell);
				if(node == null){
					return;
				}
				if (e.getClickCount() == 2) {
					addNodeRelated(node);
					refreshGraph();

				} else if (e.getClickCount() == 1) {
					try {
						Class<?> clazz = AbstractCodeArea.class;
						Field field = clazz.getDeclaredField("contentPanel");
						field.setAccessible(true);
						ContentPanel cp = (ContentPanel) field.get(JadxAiViewAction.getCodeArea());
						JNode jNode = Objects.requireNonNull(cp.getMainWindow()).getCacheObject().getNodeCache().makeFrom(node);
						cp.getTabsController().codeJump(jNode);

					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}

				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					Object[] selectedCells = graphComponent.getGraph().getSelectionCells();
					if(selectedCells.length == 0) {
						showNotSelectPopupMenu(e);
					}else{
						showPopupMenu(e);
					}
				}
			}
			private JMenuItem createAddMenuItem(String name, Map<String,JavaNode> suggestions) {
				JMenuItem menuItem = new JMenuItem(name);
				menuItem.addActionListener(e -> new DynamicSuggestionWindow(name, suggestions, new Consumer<JavaNode>() {
					@Override
					public void accept(JavaNode javaNode) {
						if (javaNode != null){
							graphStructure.addNode(javaNode);
							refreshGraph();
						}else{
							JOptionPane.showMessageDialog(graphComponent, "error:" , "not found ...", JOptionPane.ERROR_MESSAGE);
						}
					}
				}).showWindow());
				return menuItem;
			}

			private void showNotSelectPopupMenu(MouseEvent e){
				popupMenu.removeAll();
				JMenu addMenu = new JMenu("Add");
				addMenu.add(createAddMenuItem("Class", allClassNodeName));
				addMenu.add(createAddMenuItem("Method", allMethodNodeName));
				addMenu.add(createAddMenuItem("Field", allFieldNodeName));

				popupMenu.add(addMenu);
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}

			private void showPopupMenu(MouseEvent e) {
				Object[] selectedCells = graphComponent.getGraph().getSelectionCells();
				if(selectedCells.length == 0) {
					return;
				}
				popupMenu.removeAll();
				JMenuItem customPromptMenu = new JMenuItem("custom prompt.");
				customPromptMenu.addActionListener(e1 -> {
					JDialog dialog = new JDialog();
					dialog.setAlwaysOnTop(true);
					dialog.setSize(300, 150);
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					int x = (screenSize.width - dialog.getWidth()) / 2;
					int y = (screenSize.height - dialog.getHeight()) / 2;
					dialog.setLocation(x, y);
					JLabel label = new JLabel("Enter the prompt:");
					JTextField textField = new JTextField(20);
					JPanel panel = new JPanel();
					panel.add(label);
					panel.add(textField);
					JButton okButton = new JButton("ok");
					okButton.addActionListener(e22 -> {
                        String input = textField.getText();
                        QAMap qaMap = new QAMap();
                        StringBuilder promptWithCode = new StringBuilder();
                        promptWithCode.append(input).append("\n");
                        for (Object cell : selectedCells) {
                            if (graphComponent.getGraph().getModel().isVertex(cell)) {
                                promptWithCode.append(PromptGenerate.CodeWrapper(vertexDataMap.get(selectedCells[0]))).append("\n");
                            } else if (graphComponent.getGraph().getModel().isEdge(cell)) {
                                mxCell edgeCell = (mxCell) cell;
                                Object source = graphComponent.getGraph().getModel().getTerminal(edgeCell, true);
                                Object target = graphComponent.getGraph().getModel().getTerminal(edgeCell, false);
                                JavaNode sourceNode = vertexDataMap.get(source);
                                JavaNode targetNode = vertexDataMap.get(target);
                                promptWithCode.append(PromptGenerate.CodeWrapper(sourceNode)).append("\n");
                                promptWithCode.append(PromptGenerate.CodeWrapper(targetNode)).append("\n");
                            }
                        }
                        new SwingWorker<String, Void>() {
                            @Override
                            protected String doInBackground() {
                                statusBar.startWaiting();
                                qaMap.setQue(promptWithCode.toString());
                                QueAnsHistory.add(qaMap);
                                return LangchainOpenAiChatModel.ask(QueAnsHistory);
                            }
                            @Override
                            protected void done() {
                                try {
                                    qaMap.setAns(get());
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(graphComponent, "request error:" , ex.toString(), JOptionPane.ERROR_MESSAGE);
                                }finally {
                                    markdownProcessor.setMarkdown(QueAnsHistory);
                                    statusBar.stopWaiting();
                                }
                            }
                        }.execute();
						dialog.dispose();
					});
					JButton cancelButton = new JButton("cancel");
					cancelButton.addActionListener(e2 -> dialog.dispose());
					JPanel buttonPanel = new JPanel();
					buttonPanel.add(okButton);
					buttonPanel.add(cancelButton);
					dialog.add(panel, BorderLayout.CENTER);
					dialog.add(buttonPanel, BorderLayout.SOUTH);
					dialog.setVisible(true);
				});
				popupMenu.add(customPromptMenu);

				for (Map.Entry<String, Map<String, Map<PromptType, String>>> categoryEntry : promptConfigManager.getConfigMap().entrySet()) {
					String category = categoryEntry.getKey();
					Map<String, Map<PromptType, String>> items = categoryEntry.getValue();

					JMenu categoryMenu = new JMenu(category);
					for (Map.Entry<String, Map<PromptType, String>> item : items.entrySet()) {
						JMenuItem menuItem = new JMenuItem(item.getKey());
						Map<PromptType, String> promptTypeValue = item.getValue();
						menuItem.addActionListener(e12 -> {
                            StringBuilder promptWithCode = new StringBuilder();
                            if(selectedCells.length == 1 ){
                                if(graphComponent.getGraph().getModel().isVertex(selectedCells[0])){
                                    promptWithCode.append(PromptGenerate.GenerateQueByNode(vertexDataMap.get(selectedCells[0]),promptTypeValue.get(PromptType.Node)));

                                } else if (graphComponent.getGraph().getModel().isEdge(selectedCells[0])) {
                                    mxCell edgeCell = (mxCell) selectedCells[0];
                                    Object source = graphComponent.getGraph().getModel().getTerminal(edgeCell, true);
                                    Object target = graphComponent.getGraph().getModel().getTerminal(edgeCell, false);
                                    JavaNode sourceNode = vertexDataMap.get(source);
                                    JavaNode targetNode = vertexDataMap.get(target);
                                    promptWithCode.append(PromptGenerate.GenerateQueByEdge(sourceNode,targetNode,promptTypeValue.get(PromptType.Edge)));
                                }else{
                                    // error
                                }
                            }else{
                                promptWithCode.append(promptTypeValue.get(PromptType.Clutter)).append("\n");

                                for (Object cell : selectedCells) {
                                    if (graphComponent.getGraph().getModel().isVertex(cell)) {
                                        promptWithCode.append(PromptGenerate.GenerateQueByNode(vertexDataMap.get(cell),promptTypeValue.get(PromptType.Node))).append("\n");
                                    } else if (graphComponent.getGraph().getModel().isEdge(cell)) {
                                        promptWithCode.append(promptTypeValue.get(PromptType.Edge)).append("\n");
                                        mxCell edgeCell = (mxCell) cell;
                                        Object source = graphComponent.getGraph().getModel().getTerminal(edgeCell, true);
                                        Object target = graphComponent.getGraph().getModel().getTerminal(edgeCell, false);
                                        JavaNode sourceNode = vertexDataMap.get(source);
                                        JavaNode targetNode = vertexDataMap.get(target);
                                        promptWithCode.append(PromptGenerate.GenerateQueByEdge(sourceNode,targetNode,promptTypeValue.get(PromptType.Edge))).append("\n");
                                    }
                                }
                            }

                            QAMap qaMap = new QAMap();

                            new SwingWorker<String, Void>() {
                                @Override
                                protected String doInBackground() {
                                    statusBar.startWaiting();
                                    qaMap.setQue(promptWithCode.toString());
                                    QueAnsHistory.add(qaMap);
                                    return LangchainOpenAiChatModel.ask(QueAnsHistory);
                                }
                                @Override
                                protected void done() {
                                    try {
                                        qaMap.setAns(get());
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(graphComponent, "request error:" , ex.toString(), JOptionPane.ERROR_MESSAGE);
                                    }finally {
                                        markdownProcessor.setMarkdown(QueAnsHistory);
                                        statusBar.stopWaiting();
                                    }
                                }
                            }.execute();
                        }
                        );
						categoryMenu.add(menuItem);
					}
					popupMenu.add(categoryMenu);
				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private void addNodeRelated(JavaNode node){
		if (node.getUseIn() != null) {
			for (JavaNode javaNode : node.getUseIn()) {
				if(vertexMap.get(javaNode) == null){
					graphStructure.addNode(javaNode);
					graphStructure.addEdge(javaNode,node);
				}
			}
		}
		if(node instanceof JavaMethod){
			try{
				((JavaMethod) node).getMethodNode().load();
				List<MethodInfo> calledMethods = new ArrayList<>();
				for (InsnNode insn : ((JavaMethod) node).getMethodNode().getInstructions()) {
					if (insn instanceof BaseInvokeNode) {
						BaseInvokeNode invokeNode = (BaseInvokeNode) insn;
						MethodInfo calledMethodInfo = invokeNode.getCallMth();
						calledMethods.add(calledMethodInfo);
					}
				}
				for (JavaClass aClass : context.getDecompiler().getClasses()) {
					for(MethodInfo mth: calledMethods){
						if(aClass.getFullName().equals(mth.getDeclClass().getFullName())){
							for (JavaMethod method : aClass.getMethods()) {
								if(method.getFullName().equals(mth.getFullName())){
									JavaMethod callNode = method.getMethodNode().getJavaNode();
									graphStructure.addNodeEdgeFrom(callNode,node);
								}
							}
						}
					}
				}
			}catch (Exception ex){
				// 此处大概率是根本不存在这个类
				// JOptionPane.showMessageDialog(null, ex.toString());
			}
		}
	}

	public JPanel getGraphComponent() {
		return mainPanel;
	}

	public void refreshGraph() {
		Object parent = jGraphXGraph.getDefaultParent();

		jGraphXGraph.getModel().beginUpdate();
		try {
			jGraphXGraph.cellsRemoved(jGraphXGraph.getChildCells(parent, true, true));
			vertexMap.clear();
			vertexDataMap.clear();
			for (JavaNode entry : graphStructure.getNodes()) {
				String name = Helper.extractClassName(entry.getFullName());
				Object jGraphXVertex = jGraphXGraph.insertVertex(parent, null, name, 0, 0, Helper.calculateNodeWidth(name), 50,AnnotationStyles.getOrDefault(entry.getCodeNodeRef().getAnnType(),""));
				vertexMap.put(entry, jGraphXVertex);
				vertexDataMap.put(jGraphXVertex, entry);
			}
			for (GraphStructure.Edge edge : graphStructure.getEdges()) {
				Object sourceVertex = vertexMap.get(edge.getSource());
				Object targetVertex = vertexMap.get(edge.getTarget());
				if (sourceVertex != null && targetVertex != null) {
					jGraphXGraph.insertEdge(parent, null, "", sourceVertex, targetVertex);
				}
			}
			mxHierarchicalLayout layout = new mxHierarchicalLayout(jGraphXGraph);
			layout.execute(parent);
			// 高亮main node
			jGraphXGraph.setCellStyles("fillColor", "#FFDD00", new Object[]{vertexMap.get(graphStructure.getMainNode())});
		} finally {
			jGraphXGraph.getModel().endUpdate();
		}
		graphComponent.refresh();
	}

}

