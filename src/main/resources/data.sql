-- =========================
-- PRODUCT
-- =========================

INSERT INTO product (
    id, code, name, description, product_type, price, created_at, updated_at, deleted_at
)
SELECT
    1,
    'SUBSCRIPTION_MONTHLY',
    '월 구독권',
    '전문가 경매 대행 구독 상품',
    'SUBSCRIPTION',
    300000,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE code = 'SUBSCRIPTION_MONTHLY'
);

INSERT INTO product (
    id, code, name, description, product_type, price, created_at, updated_at, deleted_at
)
SELECT
    2,
    'VIEW_TICKET_10',
    '매물 열람권 10장',
    '매물 조회용 열람권 10장',
    'VIEW_TICKET',
    9900,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE code = 'VIEW_TICKET_10'
);

INSERT INTO product (
    id, code, name, description, product_type, price, created_at, updated_at, deleted_at
)
SELECT
    3,
    'PACKAGE_PREMIUM',
    '프리미엄 패키지',
    '월 구독 + 매물 열람권 30장',
    'PACKAGE',
    329000,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE code = 'PACKAGE_PREMIUM'
);


-- =========================
-- PRODUCT_COMPONENT
-- =========================

INSERT INTO product_component (
    id, component_type, name, description, created_at, updated_at, deleted_at
)
SELECT
    1,
    'SUBSCRIPTION',
    '경매 대행',
    '전문가 구독 구성요소',
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component WHERE id = 1
);

INSERT INTO product_component (
    id, component_type, name, description, created_at, updated_at, deleted_at
)
SELECT
    2,
    'VIEW_TICKET',
    '매물 열람권 10장',
    '매물 조회용 열람권 10장 구성요소',
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component WHERE id = 2
);

INSERT INTO product_component (
    id, component_type, name, description, created_at, updated_at, deleted_at
)
SELECT
    3,
    'SUBSCRIPTION',
    '경매 대행',
    '프리미엄 패키지의 구독 구성요소',
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component WHERE id = 3
);

INSERT INTO product_component (
    id, component_type, name, description, created_at, updated_at, deleted_at
)
SELECT
    4,
    'VIEW_TICKET',
    '매물 열람권 30장',
    '프리미엄 패키지의 열람권 구성요소',
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component WHERE id = 4
);


-- =========================
-- VIEW_TICKET_COMPONENT_DETAIL
-- (SUBSCRIPTION 컴포넌트는 detail 없음)
-- =========================

INSERT INTO view_ticket_component_detail (
    product_component_id, ticket_count, created_at, updated_at, deleted_at
)
SELECT
    2,
    10,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM view_ticket_component_detail WHERE product_component_id = 2
);

INSERT INTO view_ticket_component_detail (
    product_component_id, ticket_count, created_at, updated_at, deleted_at
)
SELECT
    4,
    30,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM view_ticket_component_detail WHERE product_component_id = 4
);


-- =========================
-- PRODUCT_COMPONENT_MAPPING
-- =========================

INSERT INTO product_component_mapping (
    id, product_id, product_component_id, created_at, updated_at, deleted_at
)
SELECT
    1,
    1,
    1,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component_mapping WHERE id = 1
);

INSERT INTO product_component_mapping (
    id, product_id, product_component_id, created_at, updated_at, deleted_at
)
SELECT
    2,
    2,
    2,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component_mapping WHERE id = 2
);

INSERT INTO product_component_mapping (
    id, product_id, product_component_id, created_at, updated_at, deleted_at
)
SELECT
    3,
    3,
    3,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component_mapping WHERE id = 3
);

INSERT INTO product_component_mapping (
    id, product_id, product_component_id, created_at, updated_at, deleted_at
)
SELECT
    4,
    3,
    4,
    NOW(),
    NOW(),
    NULL
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM product_component_mapping WHERE id = 4
);