define(function() {
	return function($scope, jquery, require, orchestration) {

		orchestration.invoke("navigation", "getFlashObject", "articleId",
				function(articleId) {
					$.ajax({
						type : "post",
						dataType : "json",
						url : 'infomgmt/searchById',
						data : {
							articleId : articleId
						},
						success : function(data) {
							$scope.content = data;

							if (!$scope.$$phase) {
								$scope.$apply();
							}
						}
					});
				});
	};
});