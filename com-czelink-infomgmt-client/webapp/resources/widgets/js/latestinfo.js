define(function() {
	return function($scope, jquery, require, orchestration) {

		$.getJSON('infomgmt/simple', function(data) {
			$scope.contents = data.contents;

			if (!$scope.$$phase) {
				$scope.$apply();
			}
		});
	};
});