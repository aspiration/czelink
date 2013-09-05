define(
		[ 'rangy-cssclassapplier', 'rangy-selectionsaverestore' ],
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

			var docStyleSheet = undefined;
			var textLinkRuleText = undefined;
			angular.forEach(document.styleSheets, function(elem, key) {
				if (elem.href !== null && elem.href !== undefined
						&& elem.href.indexOf("style.css") !== -1) {
					docStyleSheet = elem;
					var cssRules = docStyleSheet.cssRules;
					angular.forEach(cssRules, function(rule, key) {
						if (rule.selectorText === ".text-link") {
							textLinkRuleText = rule.style.cssText;
						}
					});
				}
			});

			var createLinkCssStyle = function(counter) {
				var cssRuleName = "text-link" + "-" + counter;
				docStyleSheet.insertRule("." + cssRuleName + "{"
						+ textLinkRuleText + "}", 0);
				return cssRuleName;
			};

			return (function() {
				var editor = {};
				
				editor.savedSelection = undefined;
				
				editor.saveSelection = function() {
					editor.savedSelection = rangy.saveSelection();
				};
				
				editor.restoreSelection = function() {
					rangy.restoreSelection(editor.savedSelection);
				};

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

				editor.createLinkManager = function() {
					var linkManagerConstructor = function() {
						var linkHolder = {};
						var instance = {};
						var counter = 0;

						instance.createLink = function(linkTitle, linkUrl) {
							var cssRuleName = createLinkCssStyle(counter);
							var linkApplier = rangy.createCssClassApplier(
									cssRuleName, {
										elementTagName : "a",
										elementProperties : {
											href : linkUrl,
											title : linkTitle
										}
									});
							linkHolder[linkTitle] = linkApplier;
							counter++;
							return instance;
						};

						instance.applyLink = function(linkTitle) {
							var linkApplier = linkHolder[linkTitle];
							var result = false;
							if (!instance.isLinkApplied(linkTitle)) {
								linkApplier.applyToSelection();
								result = true;
							}
							return result;
						};

						instance.isLinkApplied = function(linkTitle) {
							var result = false;
							var linkApplier = linkHolder[linkTitle];
							if (linkApplier !== undefined
									&& linkApplier !== null) {
								result = linkApplier.isAppliedToSelection();
							}
							return result;
						};

						instance.unLink = function(linkTitle) {
							var linkApplier = linkHolder[linkTitle];
							var result = false;
							if (instance.isLinkApplied(linkTitle)) {
								linkApplier.undoToSelection();
								result = true;
							}
							return result;
						};
						return instance;
					};
					return linkManagerConstructor();
				};

				return editor;
			})();
		});