package com.example.store.i18n;

public final class MessageKeys {

    private MessageKeys() {}

    public static final class PRODUCT {
        public static final String NOT_FOUND = "product.notFound";
        public static final String DESCRIPTION_REQUIRED = "product.description.required";
        public static final String MULTIPLE_NOT_FOUND = "product.multipleNotFound";
        private PRODUCT() {}
    }

    public static final class CUSTOMER {
        public static final String NOT_FOUND = "customer.notFound";
        public static final String FIRST_NAME_REQUIRED = "customer.firstName.required";
        public static final String LAST_NAME_REQUIRED = "customer.lastName.required";
        public static final String SEARCH_TERM_EMPTY = "customer.searchTerm.empty";
        private CUSTOMER() {}
    }

    public static final class ORDER {
        public static final String NOT_FOUND = "order.notFound";
        public static final String DESCRIPTION_REQUIRED = "order.description.required";
        public static final String CUSTOMER_ID_REQUIRED = "order.customerId.required";
        public static final String PRODUCTS_REQUIRED = "order.products.required";
        public static final String PRODUCTS_DUPLICATE = "order.products.duplicate";
        private ORDER() {}
    }

    public static final class PAGINATION {
        public static final String PAGE_NEGATIVE = "pagination.page.negative";
        public static final String SIZE_RANGE = "pagination.size.range";
        private PAGINATION() {}
    }

    public static final class ERROR {
        public static final String DATA_INTEGRITY_DUPLICATE = "error.dataIntegrity.duplicate";
        public static final String UNEXPECTED = "error.unexpected";
        public static final String VALIDATION_FAILED = "error.validationFailed";
        private ERROR() {}
    }
}
