requirejs.config({
	baseUrl : 'resources/js/lib',
	paths : {
		views : '../../views',
		widgets : '../../widgets'
	},
	shim : {
		"angular" : {
			exports : "angular"
		},
		"jquery" : {
			exports : "$"
		},
		"bootstrap" : {
			exports : "bootstrap"
		}
	}
});

requirejs([ 'angular', 'jquery', 'domReady', 'require', 'uuid' ], function(
		angular, jquery, domReady, require, uuid) {

	domReady(function() {
		// require bootstrap
		require([ 'bootstrap', 'orchestration', 'navigation' ], function(
				bootstrap, orchestration, navigation) {
			// no-operation.
		});
	});
});