package jadx.plugins.ai.utils;

import java.util.function.BiFunction;

import jadx.api.ICodeInfo;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.metadata.annotations.NodeDeclareRef;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;

// Credits Skylot: https://github.com/skylot/jadx/issues/2305#issuecomment-2409093335
public class CodeExtractor {
	public static String getMethodCode(MethodNode mth, ICodeInfo codeInfo) {
		int startPos = getCommentStartPos(codeInfo, mth.getDefPosition());
		int stopPos = getEndFrom(mth.getDefPosition(), codeInfo) + 1;
		return codeInfo.getCodeStr().substring(startPos, stopPos);
	}

	public static String getClassCode(ClassNode cls, ICodeInfo codeInfo) {
		int startPos = getCommentStartPos(codeInfo, cls.getDefPosition());
		int stopPos = getEndFrom(cls.getDefPosition(), codeInfo) + 1;
		return codeInfo.getCodeStr().substring(startPos, stopPos);
	}


	public static String getCode(JavaNode node) {
		if (node instanceof JavaClass) {
			if (((JavaClass) node).getCode().isBlank()) {
				// getCode() is blank for inner class code, extract code using different method
				return getClassCode(((JavaClass) node).getClassNode(), node.getTopParentClass().getCodeInfo());
			}
			return ((JavaClass) node).getCode();
		} else if (node instanceof JavaMethod) {
			return Helper.removeLeadingWhitespace(((JavaMethod) node).getCodeStr());
			// For Jadx 1.5.0
			// return getMethodCode(((JavaMethod) node).getMethodNode(), node.getTopParentClass().getCodeInfo());
		} else if (node instanceof JavaField) {
			return ((JavaField) node).getFieldNode().getType().toString() + " " + ((JavaField) node).getFieldNode().getFieldInfo().getDeclClass() + "." + ((JavaField) node).getFieldNode().getFieldInfo().getName();
		}
		return "";
	}

	protected static int getCommentStartPos(ICodeInfo codeInfo, int pos) {
		String emptyLine = "\n\n";
		int emptyLinePos = codeInfo.getCodeStr().lastIndexOf(emptyLine, pos);
		return emptyLinePos == -1 ? pos : emptyLinePos + emptyLine.length();
	}

	private static int getEndFrom(int startPosition, ICodeInfo codeInfo) {
		// skip nested nodes DEF/END until first unpaired END annotation (end of this method)
		Integer end = codeInfo.getCodeMetadata().searchDown(startPosition + 1, new BiFunction<>() {
			int nested = 0;

			@Override
			public Integer apply(Integer pos, ICodeAnnotation ann) {
				switch (ann.getAnnType()) {
					case DECLARATION:
						ICodeNodeRef node = ((NodeDeclareRef) ann).getNode();
						switch (node.getAnnType()) {
							case CLASS:
							case METHOD:
								nested++;
								break;
						}
						break;

					case END:
						if (nested == 0) {
							return pos;
						}
						nested--;
						break;
				}
				return null;
			}
		});
		return end != null ? end : codeInfo.getCodeStr().length();
	}
}
