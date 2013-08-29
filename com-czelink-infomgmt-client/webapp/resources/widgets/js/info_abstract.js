define(function() {
	return function($scope, jquery, require, orchestration) {
		// TODO: dummy implementation.

		$scope.createNewInfoArticle = function(newArticleTitle) {
			var options = {
				location : 'information.html',
				flashObjs : {
					new_article_title : newArticleTitle,
					info_new : true
				}
			};
			orchestration.invoke('navigation', 'navigateTo', options);
		};
	};
});