define(function() {
	return function($scope, jquery, require, orchestration) {

		jquery('.carousel').carousel({
			interval : 2000
		});
	};
});