define(function() {
	return function($scope, jquery, require, orchestration) {
		// TODO: dummy implementation.

		$scope.createNewInfoArticle = function(newArticleTitle) {

			$.getJSON('app/startConversation', function(data) {
				var uid = data.uid;

				var options = {
					location : 'information.html',
					flashObjs : {
						new_article_title : newArticleTitle,
						conversation_id : uid,
						info_new : true
					},
				};
				orchestration.invoke('navigation', 'navigateTo', options);

			});
		};
	};
});