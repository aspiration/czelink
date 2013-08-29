define([ 'rangy-cssclassapplier' ], function() {
	rangy.init();
	rangy.modules.CssClassApplier;

	var textItalicDecorator = rangy.createCssClassApplier("text-italic", {
		tagNames : [ 'span' ]
	});
	var textBoldDecorator = rangy.createCssClassApplier("text-bold", {
		tagNames : [ 'span' ]
	});
	var textUnderlineDecorator = rangy.createCssClassApplier("text-underline",
			{
				tagNames : [ 'span' ]
			});
	var textLineThroughDecorator = rangy
			.createCssClassApplier("text-line-through");

	return (function() {
		var editor = {};

		editor.italicText = function() {
			textItalicDecorator.toggleSelection();
		};

		editor.boldText = function() {
			textBoldDecorator.toggleSelection();
		};

		editor.underlineText = function() {
			textUnderlineDecorator.toggleSelection();
		};

		editor.lineThroughText = function() {
			textLineThroughDecorator.toggleSelection();
		};

		editor.createLink = function(linkTitle, linkUrl) {
			var linkApplier = rangy.createCssClassApplier("text-link", {
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