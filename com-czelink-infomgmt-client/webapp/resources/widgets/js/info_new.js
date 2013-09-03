define(
		[ 'contentEditor' ],
		function(contentEditor) {

			return function($scope, jquery, require, orchestration,
					widgetElement) {

				var articleContentArea = widgetElement
						.querySelector("div.articleContentArea");

				var isSelectedValid = function() {
					return contentEditor.isSelectedValid(articleContentArea,
							"articleeditable");
				};

				var isLocationValid = function() {
					return contentEditor.isLocationValid(articleContentArea,
							"articleeditable");
				};

				var getSelectedParagraph = function() {
					return contentEditor.getLandMarkLocation(
							articleContentArea, "articleeditable");
				};

				var underlineBtn = null;
				var linethroughBtn = null;

				var processUnderlineApplied = function() {
					if (contentEditor.isUnderlineApplied()) {
						$(underlineBtn).addClass("active");
					} else {
						$(underlineBtn).removeClass("active");
					}
				};
				var processLineThroughApplied = function() {
					if (contentEditor.isLineThroughApplied()) {
						$(linethroughBtn).addClass("active");
					} else {
						$(linethroughBtn).removeClass("active");
					}
				};

				// /**
				// * consist by: true, false. 1. true: paragraph, which can
				// follow
				// * by paragraph or picture. 2. false: picture, which can only
				// * follow by paragraph.
				// */
				// var structureStack = [];
				//
				// var isIntentedItemValid = function(intentedItem) {
				// var lastItem = structureStack[structureStack.length - 1];
				// if (lastItem === undefined || lastItem === null) {
				// lastItem = true;
				// }
				// return (intentedItem || lastItem);
				// };

				/*
				 * 1. each element in paragraph will also consists with: text,
				 * picUrl. text in paragraph will be HTML text. 2. each picture
				 * in picUrls will be put under paragraphs.
				 */
				$scope.article = {
					title : {
						text : undefined,
						picUrl : undefined
					},
					paragraphs : [],
					picUrls : []
				};

				$scope.initParagraphContent = [];

				$scope.restParagraphNum = 10;

				$scope.contentEditable = true;

				$scope.fontEditStart = function() {
					var result = false;
					if (isSelectedValid()) {
						result = true;
						processUnderlineApplied();
						processLineThroughApplied();
					}
					return result;
				};

				$scope.isInsertParagraphDisabled = false;

				$scope.insertNewParagraph = function() {
					if ($scope.restParagraphNum > 0) {
						var selectedParagraph = getSelectedParagraph();

						if (selectedParagraph === undefined) {
							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.push(newParagraph);
						} else {
							var index = parseInt(selectedParagraph
									.getAttribute("articleeditable"));

							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.splice(index + 1, 0,
									newParagraph);
						}

						$scope.restParagraphNum--;
					}
					if ($scope.restParagraphNum === 0) {
						$scope.isInsertParagraphDisabled = true;
					}
				};

				var titlePicUrl = undefined;
				var totalPicNum = 11;

				var paraPicInsertStatus = [];
				for ( var i = 0; i < (totalPicNum - 1); i++) {
					paraPicInsertStatus.push(false);
				}

				$scope.isInsertPicDisabled = false;
				$scope.restPicNum = totalPicNum;

				$scope.picInsertStatus = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph === undefined) {
						if (titlePicUrl === undefined || titlePicUrl === null) {
							$scope.isInsertPicDisabled = false;
						} else {
							$scope.isInsertPicDisabled = true;
						}
					} else {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicUrl === undefined
									|| titlePicUrl === null) {
								$scope.isInsertPicDisabled = false;
							} else {
								$scope.isInsertPicDisabled = true;
							}
						} else {
							$scope.isInsertPicDisabled = paraPicInsertStatus[index];
						}
					}
				};

				$scope.insertNewPicture = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph !== undefined) {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicUrl === undefined
									|| titlePicUrl === null) {
								titlePicUrl = ""; // TODO: to add real
								// implementation.
								console.log("instert new title pic!");
								$scope.restPicNum--;
								$scope.isInsertPicDisabled = true;
							}
						} else {
							if (!paraPicInsertStatus[index]) {
								paraPicInsertStatus[index] = true;
								// TODO: to add real implementation.
								console
										.log("insert pic for pargraph: "
												+ index);
								$scope.restPicNum--;
							}
						}
					} else {
						if (titlePicUrl === undefined || titlePicUrl === null) {
							titlePicUrl = ""; // TODO: to add real
							// implementation.
							console.log("instert new title pic!");
							$scope.restPicNum--;
							$scope.isInsertPicDisabled = true;
						}
					}
					if ($scope.restPicNum <= 0) {
						$scope.isInsertPicDisabled = true;
					}
				};

				orchestration.invoke("navigation", "getFlashObject",
						"new_article_title", function(articleTitle) {
							$scope.article.title.text = articleTitle;
						});

				$scope.links = {};

				$scope.bold = function() {
					if (isSelectedValid()) {
						contentEditor.boldText();
					}
				};

				$scope.italic = function() {
					if (isSelectedValid()) {
						contentEditor.italicText();
					}
				};

				$scope.underline = function() {
					if (isSelectedValid()) {
						contentEditor.underlineText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.lineThrough = function() {
					if (isSelectedValid()) {
						contentEditor.lineThroughText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.createLink = function(linkTitle, linkUrl) {
					// TODO: to finish.
					$scope.links[linkTitle] = createLink.lineThroughText(
							linkTitle, linkUrl);
				};

				$scope.overParagraphPanelIndex = -1;
				$scope.selectedParagraph = -1;

				$scope.checkOverParagrahIndex = function(index) {
					return $scope.overParagraphPanelIndex === index;
				};

				$scope.selectParagraph = function(index, $event) {
					$scope.selectedParagraph = index;

					// disable pic insert button.
					if (index === -1 && titlePicUrl !== undefined
							&& titlePicUrl !== null) {
						$scope.isInsertPicDisabled = true;
					}

					// stop event bubble up.
					if ($event.stopPropagation) {
						$event.stopPropagation();
					}
					if ($event.preventDefault) {
						$event.preventDefault();
					}
					$event.cancelBubble = true;
					$event.returnValue = false;
				};

				$scope.checkSelectPanel = function(index) {
					return ($scope.selectedParagraph === index);
				};

				$scope.displayParapgrahPanel = function(index) {
					return (checkSelectPanel(index) || checkOverParagrahIndex(index));
				};

				$scope.enterParagraph = function(index) {
					$scope.overParagraphPanelIndex = index;
				};

				$scope.leaveParagraph = function() {
					$scope.overParagraphPanelIndex = -1;
				};

				$scope.enterCrossClass = "icon-remove-circle";

				$scope.enterCross = function() {
					$scope.enterCrossClass = "icon-remove-sign";
				};

				$scope.leaveCross = function() {
					$scope.enterCrossClass = "icon-remove-circle";
				};

				$scope.removeParagraph = function(index) {
					$scope.initParagraphContent.splice(index, 1);
					if ($scope.restParagraphNum < 10) {
						$scope.restParagraphNum++;
						$scope.isInsertParagraphDisabled = false;

						// TODO: to add remove picture function.
					}
				};

				return function(widgetElement) {
					underlineBtn = widgetElement
							.querySelector("button[underline-btn]");
					linethroughBtn = widgetElement
							.querySelector("button[linethrough-btn]");
				};
			};
		});