define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {

		$scope.createNewInfoArticle = function(newArticleTitle) {

			secureDataRetriever
					.onSuccess(function(data) {
						var uid = data.uid;

						var confirm_conversation_interval = setInterval(
								function() {
									orchestration.invoke('info_new',
											'confirmUploadConversation', uid);
								}, 1200000);

						var options = {
							location : 'information.html',
							flashObjs : {
								new_article_title : newArticleTitle,
								conversation_id : uid,
								info_new : true,
								confirm_conversation_interval : confirm_conversation_interval
							},
						};
						orchestration.invoke('navigation', 'navigateTo',
								options);
					});

			secureDataRetriever.get("app/startUploadConversation");
		};
	};
});