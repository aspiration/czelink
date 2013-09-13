define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {

		$scope.createNewInfoArticle = function(newArticleTitle) {

			secureDataRetriever.onSuccess(function(data) {
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

			secureDataRetriever.get("app/startUploadConversation");
		};
	};
});