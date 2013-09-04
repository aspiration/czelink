define(
		[ 'contentEditor', 'dropzone', 'uuid' ],
		function(contentEditor, dropzone, uuid) {

			dropzone.autoDiscover = false;
			var imgDropzones = [];
			var initImgDropzones = function(widgetElement, fileParamName,
					fileHash) {
				var targetElement = widgetElement
						.querySelector("p[img-dropzone=" + fileParamName + "]");
				var options = {
					url : "/file/post", // TODO: to change
					dictDefaultMessage : "将图片拖入该区域上传 或 点击这里上传图片 （只支持上传一张图片）",
					dictFallbackMessage : "请使用以下浏览器： Chrome 7+ / Firefox 4+ / IE 10+ / Opera 12+ / Safari 6+ ",
					dictInvalidFileType : "不支持的文件类型",
					dictFileTooBig : "文件太大",
					dictResponseError : "文件上传失败, 如需要请联系管理员",
					dictMaxFilesExceeded : "只可以添加一个文件",
					maxFilesize : 2,
					paramName : fileHash,
					maxFiles : 1,
					headers : {
						"file-hashcode" : fileHash
					},
					addRemoveLinks : true,
					dictCancelUpload : "取消上传",
					dictRemoveFile : "删除文件",
					acceptedFiles : "image/*",
					init : function() {
						this
								.on(
										"error",
										function(file) {
											this.removeFile(file);
											var customMessageArea = this.element.parentNode
													.querySelector("p[dz-custom-message='error']");
											customMessageArea
													.querySelector("span[message]").textContent = "上传失败";
											customMessageArea
													.querySelector("button[class='close']").onclick = function() {
												customMessageArea.setAttribute(
														"hidden", true);
											};
											customMessageArea
													.removeAttribute("hidden");
										});

						this
								.on(
										"success",
										function(file) {
											var customMessageArea = this.element.parentNode
													.querySelector("p[dz-custom-message='success']");
											customMessageArea
													.querySelector("span[message]").textContent = "上传成功";
											customMessageArea
													.querySelector("button[class='close']").onclick = function() {
												customMessageArea.setAttribute(
														"hidden", true);
											};
											customMessageArea
													.removeAttribute("hidden");
										});
					}
				};
				var newDrop = new dropzone(targetElement, options);
				var customMessageArea = newDrop.element.parentNode
						.querySelector("p[dz-custom-message]");
				customMessageArea.setAttribute("hidden", true);
				newDrop.disable();
				imgDropzones.push(newDrop);
			};

			return function($scope, jquery, require, orchestration,
					widgetElement) {

				var articleContentArea = widgetElement
						.querySelector("div.articleContentArea");

				var isSelectedValid = function() {
					return contentEditor.isSelectedValid(articleContentArea,
							"articleeditable");
				};

				var isLocationValid = function() {
					return contentEditor.isLocationValid(articleContentArea,
							"articleeditable");
				};

				var getSelectedParagraph = function() {
					return contentEditor.getLandMarkLocation(
							articleContentArea, "articleeditable");
				};

				var underlineBtn = null;
				var linethroughBtn = null;

				var processUnderlineApplied = function() {
					if (contentEditor.isUnderlineApplied()) {
						$(underlineBtn).addClass("active");
					} else {
						$(underlineBtn).removeClass("active");
					}
				};
				var processLineThroughApplied = function() {
					if (contentEditor.isLineThroughApplied()) {
						$(linethroughBtn).addClass("active");
					} else {
						$(linethroughBtn).removeClass("active");
					}
				};

				/*
				 * 1. each element in paragraph will also consists with: text,
				 * picUrl. text in paragraph will be HTML text. 2. each picture
				 * in picUrls will be put under paragraphs.
				 */
				$scope.article = {
					title : {
						text : undefined,
						picUrl : undefined
					},
					paragraphs : [],
					picUrls : []
				};

				$scope.initParagraphContent = [];

				$scope.restParagraphNum = 10;

				$scope.contentEditable = true;

				$scope.fontEditStart = function() {
					var result = false;
					if (isSelectedValid()) {
						result = true;
						processUnderlineApplied();
						processLineThroughApplied();
					}
					return result;
				};

				$scope.isInsertParagraphDisabled = false;

				$scope.insertNewParagraph = function() {
					if ($scope.restParagraphNum > 0) {
						var selectedParagraph = getSelectedParagraph();

						if (selectedParagraph === undefined) {
							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.push(newParagraph);
						} else {
							var index = parseInt(selectedParagraph
									.getAttribute("articleeditable"));

							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.splice(index + 1, 0,
									newParagraph);
						}

						$scope.restParagraphNum--;
					}
					if ($scope.restParagraphNum === 0) {
						$scope.isInsertParagraphDisabled = true;
					}
				};

				var titlePicInsertMark = undefined;
				var totalPicNum = 11;

				var paraPicInsertStatus = [];
				for ( var i = 0; i < (totalPicNum - 1); i++) {
					paraPicInsertStatus.push(false);
				}

				$scope.isInsertPicDisabled = false;
				$scope.restPicNum = totalPicNum;

				$scope.picInsertStatus = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph === undefined) {
						if (titlePicInsertMark === undefined
								|| titlePicInsertMark === null) {
							$scope.isInsertPicDisabled = false;
						} else {
							$scope.isInsertPicDisabled = true;
						}
					} else {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicInsertMark === undefined
									|| titlePicInsertMark === null) {
								$scope.isInsertPicDisabled = false;
							} else {
								$scope.isInsertPicDisabled = true;
							}
						} else {
							$scope.isInsertPicDisabled = paraPicInsertStatus[index];
						}
					}
				};

				$scope.insertNewPicture = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph !== undefined) {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicInsertMark === undefined
									|| titlePicInsertMark === null) {
								titlePicInsertMark = "";
								var customMessageArea = imgDropzones[0].element.parentNode
										.querySelector("p[dz-custom-message]");
								customMessageArea.setAttribute("hidden", true);
								imgDropzones[0].removeAllFiles();
								imgDropzones[0].enable();
								$scope.restPicNum--;
								$scope.isInsertPicDisabled = true;
							}
						} else {
							if (!paraPicInsertStatus[index]) {
								paraPicInsertStatus[index] = true;
								var customMessageArea = imgDropzones[index + 1].element.parentNode
										.querySelector("p[dz-custom-message]");
								customMessageArea.setAttribute("hidden", true);
								imgDropzones[index + 1].removeAllFiles();
								imgDropzones[index + 1].enable();
								$scope.restPicNum--;
							}
						}
					} else {
						if (titlePicInsertMark === undefined
								|| titlePicInsertMark === null) {
							titlePicInsertMark = "";
							var customMessageArea = imgDropzones[0].element.parentNode
									.querySelector("p[dz-custom-message]");
							customMessageArea.setAttribute("hidden", true);
							imgDropzones[0].removeAllFiles();
							imgDropzones[0].enable();
							$scope.restPicNum--;
							$scope.isInsertPicDisabled = true;
						}
					}
					if ($scope.restPicNum <= 0) {
						$scope.isInsertPicDisabled = true;
					}
				};

				$scope.displayTitlePicZone = function() {
					var result = false;
					if (titlePicInsertMark !== undefined
							&& titlePicInsertMark !== null) {
						result = true;
					}
					return result;
				};

				$scope.cancelTitlePicZone = function() {
					titlePicInsertMark = undefined;
					$scope.isInsertPicDisabled = false;
					imgDropzones[0].disable();
					imgDropzones[0].removeAllFiles();
					$scope.restPicNum++;
				};

				$scope.displayParagraphPicZone = function(index) {
					return paraPicInsertStatus[index];
				};

				$scope.cancelParagraphPicZone = function(index) {
					imgDropzones[index + 1].disable();
					imgDropzones[index + 1].removeAllFiles();
					// TODO: fire backend to delete the uploaded file.
					paraPicInsertStatus[index] = false;
					$scope.restPicNum++;
				};

				orchestration.invoke("navigation", "getFlashObject",
						"new_article_title", function(articleTitle) {
							$scope.article.title.text = articleTitle;
						});

				$scope.links = {};

				$scope.bold = function() {
					if (isSelectedValid()) {
						contentEditor.boldText();
					}
				};

				$scope.italic = function() {
					if (isSelectedValid()) {
						contentEditor.italicText();
					}
				};

				$scope.underline = function() {
					if (isSelectedValid()) {
						contentEditor.underlineText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.lineThrough = function() {
					if (isSelectedValid()) {
						contentEditor.lineThroughText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.createLink = function(linkTitle, linkUrl) {
					// TODO: to finish.
					$scope.links[linkTitle] = createLink.lineThroughText(
							linkTitle, linkUrl);
				};

				$scope.overParagraphPanelIndex = -1;
				$scope.selectedParagraph = -1;

				$scope.checkOverParagrahIndex = function(index) {
					return $scope.overParagraphPanelIndex === index;
				};

				$scope.selectParagraph = function(index, $event) {
					$scope.selectedParagraph = index;

					// disable pic insert button.
					if (index === -1 && titlePicInsertMark !== undefined
							&& titlePicInsertMark !== null) {
						$scope.isInsertPicDisabled = true;
					}

					// stop event bubble up.
					if ($event.stopPropagation) {
						$event.stopPropagation();
					}
					if ($event.preventDefault) {
						$event.preventDefault();
					}
					$event.cancelBubble = true;
					$event.returnValue = false;
				};

				$scope.checkSelectPanel = function(index) {
					return ($scope.selectedParagraph === index);
				};

				$scope.displayParapgrahPanel = function(index) {
					return (checkSelectPanel(index) || checkOverParagrahIndex(index));
				};

				$scope.enterParagraph = function(index) {
					$scope.overParagraphPanelIndex = index;
				};

				$scope.leaveParagraph = function() {
					$scope.overParagraphPanelIndex = -1;
				};

				$scope.enterCrossClass = "icon-remove-circle";

				$scope.enterCross = function() {
					$scope.enterCrossClass = "icon-remove-sign";
				};

				$scope.leaveCross = function() {
					$scope.enterCrossClass = "icon-remove-circle";
				};

				$scope.removeParagraph = function(index) {
					$scope.initParagraphContent.splice(index, 1);
					if ($scope.restParagraphNum < 10) {
						$scope.restParagraphNum++;
						$scope.isInsertParagraphDisabled = false;

						// TODO: to add remove picture function.
					}
				};

				$scope.initAbstractTitleImage = function() {
					var fileHash = uuid();
					var fileName = "abstract_title_image";
					initImgDropzones(widgetElement, fileName, fileHash);
				};

				$scope.preProcessImageDropzone = function(index, paragraph) {
					var fileHash = uuid();
					var fileName = "upload_img_file_" + index;
					paragraph.imgdesc = {};
					paragraph.imgdesc.hash = fileHash;
					paragraph.imgdesc.name = fileName;
				};

				$scope.postProcessImageDropzone = function(element, context) {
					var fileParamName = $scope.initParagraphContent[context.$index].imgdesc.name;
					var fileHash = $scope.initParagraphContent[context.$index].imgdesc.hash;
					initImgDropzones(widgetElement, fileParamName, fileHash);
				};

				return function(widgetElement) {
					underlineBtn = widgetElement
							.querySelector("button[underline-btn]");
					linethroughBtn = widgetElement
							.querySelector("button[linethrough-btn]");
				};
			};
		});