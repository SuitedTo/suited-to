

'use strict';

//simple pdf-to-image/canvas converter.  
//caches docs after parsing for better performance.
//loads main pdf code dynamically (and asynchronously) to avoid unnecessary loads.
//please disregard the large number of method signatures... ;D
var PdfRenderer = (function() {
	
	var documentCache = {}, divCache = {}, currentDivWrapper = null, ready = false, afterReady = null;

	var renderPage = function (div, pdf, pageNumber, scale, callback) {
		pdf.getPage(pageNumber).then(function(page) {
	    
			var viewport = page.getViewport(scale);

		    var pageDisplayWidth = viewport.width;
		    var pageDisplayHeight = viewport.height;
	
		    var pageDivHolder = document.createElement('div');
		    pageDivHolder.className = 'pdfpage';
		    pageDivHolder.style.width = pageDisplayWidth + 'px';
		    pageDivHolder.style.height = pageDisplayHeight + 'px';
		    div.appendChild(pageDivHolder);
	
		    // Prepare canvas using PDF page dimensions
		    var canvas = document.createElement('canvas');
		    var context = canvas.getContext('2d');
		    canvas.width = pageDisplayWidth;
		    canvas.height = pageDisplayHeight;
		    pageDivHolder.appendChild(canvas);
	
	
		    // Render PDF page into canvas context
		    var renderContext = {
		      canvasContext: context,
		      viewport: viewport
		    };
		    page.render(renderContext).then(callback);
	    
		});
	}
	
	var renderAll = function (pdf, pdfDiv, scale) {
		
		// Rendering all pages starting from first
		var pageNumber = 1;
		renderPage(pdfDiv, pdf, pageNumber++, scale, function pageRenderingComplete() {
			if (pageNumber > pdf.numPages) {
				return; // All pages rendered
		    }
		    // Continue rendering of the next page
		    renderPage(pdfDiv, pdf, pageNumber++, scale, pageRenderingComplete);
		});
	}
	
	return {
		
		//used to load pdf.js dynamically and only when needed since its a large file ~800KB
		//uses success callback to notify caller of initialization failure/success
		initialize : function _initialize(scriptDir, successCallback) {
			
			$.getScript(scriptDir + 'pdf-compatibility.js', function () {
				$.getScript(scriptDir + 'pdf-min.js', function () {
					PDFJS.disableWorker = true;
					PDFJS.workerSrc = scriptDir + 'pdf-min.js';
					successCallback(true);
					ready = true;
					if (afterReady != null) { //make a semi-faithful (will work 99.9% of the time) attempt to honor rendering calls made before loading completes
						afterReady();
					}
				}).fail(function() {
					successCallback(false);
				});
			}).fail(function() {
				successCallback(false);
			});
			
		},
		
		renderDocument : function _renderDocument(pdfUrl, pdfDiv, scale, afterParsing) {
			
			if (!ready) {
				afterReady = function () {_renderDocument(pdfUrl, pdfDiv, scale, afterParsing)};
				return;
			}
			
			currentDivWrapper = divCache[pdfUrl + scale];
			
			if (currentDivWrapper != null) {
				pdfDiv.appendChild(currentDivWrapper.div);
				afterParsing(currentDivWrapper.scrollTop);
				return;
			}
			
			var newDiv = document.createElement('div');
			pdfDiv.appendChild(newDiv);
			var cachedDoc = documentCache[pdfUrl];
			
			if (cachedDoc != null) {
				renderAll(cachedDoc, newDiv, scale);
				divCache[pdfUrl + scale] = currentDivWrapper = {div: newDiv, scrollTop: 0};
				afterParsing(currentDivWrapper.scrollTop);
				return;
			}
			
			// Fetch the PDF document from the URL using promices
			PDFJS.getDocument(pdfUrl).then(function getPdfForm(pdf) {
				
				documentCache[pdfUrl] = pdf;
				divCache[pdfUrl + scale] = currentDivWrapper = {div: newDiv, scrollTop: 0};
				afterParsing(currentDivWrapper.scrollTop); //calls back after parsing, before rendering
				renderAll(pdf, newDiv, scale);
				
			});
			
		},
		
		saveScroll : function _saveScroll(scrollTop) {
			currentDivWrapper.scrollTop = scrollTop;
		}
		
	};
	
})();