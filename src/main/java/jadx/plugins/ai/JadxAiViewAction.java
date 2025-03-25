package jadx.plugins.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.data.CommentStyle;
import jadx.api.data.ICodeComment;
import jadx.api.data.impl.JadxCodeComment;
import jadx.api.data.impl.JadxCodeData;
import jadx.api.data.impl.JadxNodeRef;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.plugins.JadxPluginContext;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.exceptions.DecodeException;
import jadx.gui.device.debugger.smali.Smali;
import jadx.gui.plugins.context.GuiPluginContext;
import jadx.gui.settings.JadxProject;
import jadx.gui.ui.codearea.CodeArea;
import jadx.plugins.ai.ai.LangchainOpenAiChatModel;
import jadx.plugins.ai.module.GraphStructure;
import jadx.plugins.ai.ui.FlowchartUI;
import jadx.plugins.ai.utils.CodeExtractor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JadxAiViewAction {

	public static JadxPluginContext context;

	public static String RENAME_CLASS = "Please optimize the following class name and return only the function name to me, and do not return anything else";
	public static String RENAME_METHOD = "Please optimize the function name of the following function and return only the function name to me, and do not return anything else.";
	public static String COMMENT_CLASS = "Please write a detailed comment for the following class and return only the comment content. The returned content should meet the Java code comment style, but there should be no comment annotations. Only return the plain text of the comment to me, not the markdown format.";
	public static String COMMENT_METHOD = "Please write a detailed comment for the following method and return only the comment content. The returned content should meet the Java code comment style, but there should be no comment annotations. Only return the plain text of the comment to me, not the markdown format.";
	public static String DECOMPILE_METHOD = "Please decompile the following Smail code into Java code, ensuring that the code logic is exactly the same. Only return the Java code of this function, do not return any other content, do not return the markdown format, only return the code. Remember that the code needs to be decompiled exactly the same.";

	public static String RENAME_ALL_METHOD = "What is returned to me must be pure JSON content, and the JSON is stored in the form of key-value pairs. The key name is the original method name, and the key value is the optimized method name. Make sure that the returned JSON string is in the above format. Do not return it to me in markdown format, do not include `, just a pure JSON string. This is the command.";

	public static void addToPopupMenu(JadxPluginContext context) {
		JadxAiViewAction.context = context;
		if (context.getGuiContext() == null) {
			return;
		}

		context.getGuiContext().addPopupMenuAction("Ai Analyze", ref -> ref.getAnnType() == ICodeAnnotation.AnnType.METHOD
				|| (ref.getAnnType() == ICodeAnnotation.AnnType.CLASS)
				|| (ref.getAnnType() == ICodeAnnotation.AnnType.FIELD), null, iCodeNodeRef -> {
			JavaNode node = context.getDecompiler().getJavaNodeByRef(iCodeNodeRef);
			GraphStructure gs = new GraphStructure(node);
			new FlowchartUI().show(context, gs);
		});

		context.getGuiContext().addPopupMenuAction("Ai Rename", ref -> ref.getAnnType() == ICodeAnnotation.AnnType.METHOD
				|| (ref.getAnnType() == ICodeAnnotation.AnnType.CLASS), null, ref -> {
			JavaNode node = context.getDecompiler().getJavaNodeByRef(ref);
			String code = CodeExtractor.getCode(node);
			if (ref.getAnnType() == ICodeAnnotation.AnnType.METHOD) {
				String s = LangchainOpenAiChatModel.ask(RENAME_METHOD + "\n" + code).trim();
				((MethodNode) ref).rename(s);
			} else {
				String s = LangchainOpenAiChatModel.ask(RENAME_CLASS + "\n" + code).trim();
				((ClassNode) ref).getClassInfo().changeShortName(s);
			}
			context.getGuiContext().applyNodeRename(ref);
		});

		context.getGuiContext().addPopupMenuAction("Ai Rename All Method", ref -> (ref.getAnnType() == ICodeAnnotation.AnnType.CLASS),
				null, ref -> {
			JavaNode node = context.getDecompiler().getJavaNodeByRef(ref);
			String code = CodeExtractor.getCode(node);
			ClassNode clsNode = (ClassNode) ref;
			String s = LangchainOpenAiChatModel.ask(RENAME_ALL_METHOD + "\n" + code).trim();
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				Map<String,MethodNode> nodeSmap = new HashMap<>();
				assert clsNode != null;
				for (MethodNode method : clsNode.getMethods()) {
					nodeSmap.put(method.getName(), method);
				}
				Map<String, Object> map = objectMapper.readValue(s, Map.class);
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String mthName = entry.getKey();
					MethodNode methodNode = nodeSmap.get(mthName);
					if(methodNode == null){
						// 此处匹配错误。
						continue;
					}
					methodNode.rename((String) entry.getValue());
					context.getGuiContext().applyNodeRename(methodNode);
				}
			} catch (Exception e) {
				throw new RuntimeException("error:\n"+s);
			}
		});

		context.getGuiContext().addPopupMenuAction("Ai Comment", ref -> ref.getAnnType() == ICodeAnnotation.AnnType.METHOD
				|| (ref.getAnnType() == ICodeAnnotation.AnnType.CLASS), null, iCodeNodeRef -> {
			JavaNode node = context.getDecompiler().getJavaNodeByRef(iCodeNodeRef);
			assert node != null;
			String code = CodeExtractor.getCode(node);
			String s;
			if (iCodeNodeRef.getAnnType() == ICodeAnnotation.AnnType.METHOD) {
				 s = LangchainOpenAiChatModel.ask(COMMENT_METHOD + "\n" + code);
			} else {
				 s = LangchainOpenAiChatModel.ask(COMMENT_CLASS + "\n" + code);
			}
			addComment(node,s,CommentStyle.BLOCK);
		});

		context.getGuiContext().addPopupMenuAction("Ai Decompile", ref -> ref.getAnnType() == ICodeAnnotation.AnnType.METHOD, null, iCodeNodeRef -> {
			JavaMethod node = (JavaMethod) context.getDecompiler().getJavaNodeByRef(iCodeNodeRef);
			assert node != null;
			try {
				node.getMethodNode().load();
			} catch (DecodeException e) {
				throw new RuntimeException(e);
			}
			Smali smail= Smali.disassemble(node.getDeclaringClass().getClassNode());
			int methodDefPos = smail.getMethodDefPos(node.getMethodNode().getMethodInfo().getRawFullId());
			String code1 = smail.getCode();
			String endMethod = ".end method";
			int endMethodPos = code1.indexOf(endMethod, methodDefPos);
			String code = code1.substring(methodDefPos,endMethodPos+endMethod.length());
			String s = LangchainOpenAiChatModel.ask(DECOMPILE_METHOD + "\n" + code);
			addComment(node,s,CommentStyle.BLOCK);
		});

	}

	public static CodeArea getCodeArea() throws Exception {
		GuiPluginContext pc = (GuiPluginContext) context.getGuiContext();
		assert pc != null;
		Class<?> clazz = pc.getClass();
		Method getCodeArea = clazz.getDeclaredMethod("getCodeArea");
		getCodeArea.setAccessible(true);
		return (CodeArea) getCodeArea.invoke(pc);
	}

	public static void addComment(JavaNode defNode,String comment, CommentStyle style){

		try {
			CodeArea codeArea = getCodeArea();

			JadxCodeComment jadxCodeComment = new JadxCodeComment(JadxNodeRef.forJavaNode(defNode), comment,style);

			JadxProject project = codeArea.getProject();
			JadxCodeData codeData = project.getCodeData();
			if (codeData == null) {
				codeData = new JadxCodeData();
			}
			List<ICodeComment> list = new ArrayList<>(codeData.getComments());
			list.add(jadxCodeComment);
			Collections.sort(list);
			codeData.setComments(list);
			project.setCodeData(codeData);
			codeArea.getMainWindow().getWrapper().reloadCodeData();
			codeArea.refreshClass();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
