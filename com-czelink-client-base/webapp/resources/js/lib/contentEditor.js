define(
		[ 'rangy-cssclassapplier' ],
		function() {
			rangy.init();
			rangy.modules.CssClassApplier;

			var textItalicDecorator = rangy.createCssClassApplier(
					"text-italic", {
						tagNames : [ 'span' ]
					});
			var textBoldDecorator = rangy.createCssClassApplier("text-bold", {
				tagNames : [ 'span' ]
			});
			var textUnderlineDecorator = rangy.createCssClassApplier(
					"text-underline", {
						tagNames : [ 'span' ]
					});
			var textLineThroughDecorator = rangy.createCssClassApplier(
					"text-line-through", {
						tagNames : [ 'span' ]
					});

			var checkSelectedItemRange = function(rootElement, landmark,
					targetElement) {
				
				var result = false;
				if (targetElement === null || targetElement === undefined) {
					result = false;
				} else if (rootElement.isEqualNode(targetElement)) {
					result = false;
				} else if (targetElement.getAttribute(landmark) === undefined
						|| targetElement.getAttribute(landmark) === null) {
					result = checkSelectedItemRange(rootElement, landmark,
							targetElement.parentElement);
				} else {
					result = true;
				}
				return result;
			};

			var getSelectedItemLandMarkElement = function(rootElement,
					landmark, targetElement) {
				var result = undefined;
				if (targetElement === null || targetElement === undefined) {
					result = undefined;
				} else if (rootElement.isEqualNode(targetElement)) {
					result = undefined;
				} else if (targetElement.getAttribute(landmark) === undefined
						|| targetElement.getAttribute(landmark) === null) {
					result = getSelectedItemLandMarkElement(rootElement,
							landmark, targetElement.parentElement);
				} else {
					result = targetElement;
				}
				return result;
			};

			return (function() {
				var editor = {};

				editor.isSelectedValid = function(rootElement, landmark) {
					var result = false;
					var htmlText = rangy.getSelection().toHtml();
					if (htmlText === "") {
						result = false;
					} else {
						result = checkSelectedItemRange(rootElement, landmark,
								rangy.getSelection().anchorNode.parentNode);
					}
					return result;
				};

				editor.isLocationValid = function(rootElement, landmark) {
					var result = false;
					var anchorNode = rangy.getSelection().anchorNode;
					if (anchorNode !== undefined && anchorNode !== null) {
						result = checkSelectedItemRange(rootElement, landmark,
								anchorNode.parentNode);
					}
					return result;
				};

				editor.getLandMarkLocation = function(rootElement, landmark) {
					var result = undefined;
					var anchorNode = rangy.getSelection().anchorNode;
					if (anchorNode !== undefined && anchorNode !== null) {
						result = getSelectedItemLandMarkElement(rootElement,
								landmark, anchorNode.parentNode);
					}
					return result;
				};

				editor.italicText = function() {
					textItalicDecorator.toggleSelection();
				};

				editor.boldText = function() {
					textBoldDecorator.toggleSelection();
				};

				editor.isUnderlineApplied = function() {
					return textUnderlineDecorator.isAppliedToSelection(rangy);
				};

				editor.underlineText = function() {
					if (textUnderlineDecorator.isAppliedToSelection(rangy)) {
						textUnderlineDecorator.undoToSelection();
					} else {
						textLineThroughDecorator.undoToSelection();
						textUnderlineDecorator.applyToSelection();
					}
				};

				editor.isLineThroughApplied = function() {
					return textLineThroughDecorator.isAppliedToSelection(rangy);
				};

				editor.lineThroughText = function() {
					if (textLineThroughDecorator.isAppliedToSelection(rangy)) {
						textLineThroughDecorator.undoToSelection();
					} else {
						textUnderlineDecorator.undoToSelection();
						textLineThroughDecorator.applyToSelection();
					}
				};

				editor.createLink = function(linkTitle, linkUrl) {
					var linkApplier = rangy.createCssClassApplier("text-link",
							{
								elementTagName : "a",
								elementProperties : {
									href : linkUrl,
									title : linkTitle
								}
							});
					linkApplier.toggleSelection();
					return linkApplier;
				};

				return editor;
			})();
		});