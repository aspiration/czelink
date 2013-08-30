define([ 'contentEditor' ],
		function(contentEditor) {

			var initParagraphItem = {
				text : "请在这里替换内容",
				contentEditable : true
			};
			var initParagraphContent = [];

			for ( var i = 0; i < 10; i++) {
				initParagraphContent.push(initParagraphItem);
			}

			return function($scope, jquery, require, orchestration,
					widgetElement) {

				var articleContentArea = widgetElement
						.querySelector("div.articleContentArea");

				var isSelectedValid = function() {
					return contentEditor.isSelectedValid(articleContentArea,
							"articleeditable");
				};

				var paragraphHolder = null;

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

				/**
				 * consist by: true, false. 1. true: paragraph, which can follow
				 * by paragraph or picture. 2. false: picture, which can only
				 * follow by paragraph.
				 */
				var structureStack = [];

				var isIntentedItemValid = function(intentedItem) {
					var lastItem = structureStack[structureStack.length - 1];
					if (lastItem === undefined || lastItem === null) {
						lastItem = true;
					}
					return (intentedItem || lastItem);
				};

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

				$scope.initParagraphContent = initParagraphContent;

				$scope.restParagraphNum = initParagraphContent.length;

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

				$scope.isInsertPicDisabled = false;
				$scope.isInsertParagraphDisabled = false;

				$scope.insertNewParagraph = function() {
					if ($scope.restParagraphNum > 0) {
						$scope.isInsertPicDisabled = false;
						structureStack.push(true);
						var pIndex = initParagraphContent.length
								- $scope.restParagraphNum;
						paragraphHolder[pIndex].removeAttribute("hidden");
						$scope.restParagraphNum--;
					}
					if ($scope.restParagraphNum === 0) {
						$scope.isInsertParagraphDisabled = true;
					}
				};

				$scope.insertNewPicture = function() {
					$scope.isInsertPicDisabled = true;

					if (isIntentedItemValid(false)) {
						structureStack.push(false);
						alert("inserted image!");
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

				return function(widgetElement) {
					paragraphHolder = widgetElement
							.querySelectorAll("div[articleeditable]");
					underlineBtn = widgetElement
							.querySelector("button[underline-btn]");
					linethroughBtn = widgetElement
							.querySelector("button[linethrough-btn]");
				};
			};
		});