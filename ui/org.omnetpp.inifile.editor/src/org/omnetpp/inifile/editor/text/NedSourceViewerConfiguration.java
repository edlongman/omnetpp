package org.omnetpp.inifile.editor.text;


import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorPart;
import org.omnetpp.inifile.editor.text.assist.NedCompletionProcessor;
import org.omnetpp.inifile.editor.text.assist.NedContentAssistPartitionScanner;
import org.omnetpp.inifile.editor.text.highlight.NedCodeColorizerScanner;
import org.omnetpp.inifile.editor.text.highlight.NedDocColorizerScanner;
import org.omnetpp.inifile.editor.text.highlight.NedPrivateDocColorizerScanner;
import org.omnetpp.inifile.editor.text.highlight.InifileSyntaxHighlightPartitionScanner;
import org.omnetpp.inifile.editor.text.util.NedAnnotationHover;
import org.omnetpp.inifile.editor.text.util.NedTextHover;

/**
 * Configuration for an <code>SourceViewer</code> which shows NED code.
 */
//XXX TODO rename, revise, possibly remove...
public class NedSourceViewerConfiguration extends SourceViewerConfiguration {

	private IEditorPart editor = null; // because NEDReconcileStrategy will need IFile from editorInput

	public NedSourceViewerConfiguration(IEditorPart editor) {
		this.editor = editor;
	}
	
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new NedAnnotationHover();
	}
		
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return null;
	}
	
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(NedContentAssistPartitionScanner.PARTITIONING_ID);
		assistant.setContentAssistProcessor(new NedCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
        
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}
	
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		return (NedContentAssistPartitionScanner.NED_DOC.equals(contentType) ? "// " : null); //$NON-NLS-1$
	}
	
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new DefaultTextDoubleClickStrategy();
	}
	
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        // TODO indentation should be configurable
		return new String[] { "\t", "    " }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        
		PresentationReconciler reconciler= new PresentationReconciler();
        // syntax highlighting is using a separate partitioner
		reconciler.setDocumentPartitioning(InifileSyntaxHighlightPartitionScanner.PARTITIONING_ID);

        // colorizers for ned code
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new NedCodeColorizerScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        // colorizer for normal ned doc 
		dr = new DefaultDamagerRepairer(new NedDocColorizerScanner());
		reconciler.setDamager(dr, InifileSyntaxHighlightPartitionScanner.INI_COMMENT);
		reconciler.setRepairer(dr, InifileSyntaxHighlightPartitionScanner.INI_COMMENT);
        
		return reconciler;
	}
	
	public int getTabWidth(ISourceViewer sourceViewer) {
        // TODO should be configurable
		return 4;
	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new NedTextHover(editor);
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		// Installs background NED parsing.
		// Based on: JavaSourceViewerConfiguration.getReconciler() in JDT which
		// creates and configures JavaReconciler; than in turn will eventually
		// result in calls to org.eclipse.jdt.internal.compiler.parser.Parser.
		MonoReconciler reconciler = new MonoReconciler(new NEDReconcileStrategy(editor), true);
		reconciler.setIsIncrementalReconciler(false);
		reconciler.setIsAllowedToModifyDocument(false);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		reconciler.setDelay(500);
		return reconciler;
	}
}
