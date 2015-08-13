logger.log("Getting content from " + args.url);
try {
    model.content = jsConsoleResources.getResourceContent(args.url);
} catch (ex) {
    logger.warn(ex);
    status.code = 404;
    status.message = ex;
    status.redirect = true;
}
