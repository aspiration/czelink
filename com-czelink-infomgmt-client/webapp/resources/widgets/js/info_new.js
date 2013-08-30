define([ 'contentEditor' ], function(contentEditor) {

	return function($scope, jquery, require, orchestration) {

		/**
		 * consist by: true, false. 1. true: paragraph, which can follow by
		 * paragraph or picture. 2. false: picture, which can only follow by
		 * paragraph.
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
		 * 1. each element in paragraph will also consists with: text, picUrl.
		 * text in paragraph will be HTML text. 2. each picture in picUrls will
		 * be put under paragraphs.
		 */
		$scope.article = {
			title : {
				text : undefined,
				picUrl : undefined
			},
			paragraphs : [],
			picUrls : []
		};

		$scope.isInsertPicDisabled = false;

		$scope.isInsertPicDisabledAttr = " ";

		$scope.insertNewParagraph = function() {
			$scope.isInsertPicDisabled = false;
			$scope.isInsertPicDisabledAttr = " ";
			structureStack.push(true);
			alert("inserted paragraph!");
		};

		$scope.insertNewPicture = function() {
			$scope.isInsertPicDisabled = true;
			$scope.isInsertPicDisabledAttr = "disabled";

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
			contentEditor.boldText();
		};

		$scope.underline = function() {
			contentEditor.underlineText();
		};

		$scope.italic = function() {
			contentEditor.italicText();
		};

		$scope.lineThrough = function() {
			contentEditor.lineThroughText();
		};

		$scope.createLink = function(linkTitle, linkUrl) {
			$scope.links[linkTitle] = createLink.lineThroughText(linkTitle,
					linkUrl);
		};
	};
});