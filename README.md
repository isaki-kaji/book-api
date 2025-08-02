### API エンドポイント

- `POST /api/authors` - 著者登録
- `PUT /api/authors/{id}` - 著者更新
- `POST /api/books` - 書籍登録（著者も同時登録可能）
- `PUT /api/books/{id}` - 書籍更新（タイトル、価格、著者）
- `PATCH /api/books/{id}/publication-status` - 出版状況更新
- `GET /api/authors/{id}/books` - 著者の書籍一覧取得

## セットアップ・実行手順

### 前提条件

- Java 21
- Docker & Docker Compose

### 2. 初回セットアップ（jOOQ コード生成）

```bash
# アプリケーションを起動（データベースコンテナも自動起動）
./gradlew bootRun

# 別ターミナルで、データベース起動を待ってからjOOQコード生成を実行
# ※ 初回のみ必要
sleep 10 && ./gradlew jooqCodegen
```

**重要**: 初回起動時は、PostgreSQL コンテナの起動と Flyway マイグレーションの完了を待ってから`./gradlew jooqCodegen`を実行してください。

### 3. 通常の起動

```bash
./gradlew bootRun
```

## API 使用例

### 書籍登録

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "吾輩は猫である",
    "price": 500,
    "authors": [
      {
        "name": "夏目漱石",
        "birthDate": "1867-02-09"
      }
    ],
    "publicationStatus": "UNPUBLISHED"
  }'
```

### 著者の書籍一覧取得

```bash
curl http://localhost:8080/api/authors/1/books
```

## 開発

### ディレクトリ構成

```
src/main/kotlin/com/isaki_kaji/book_api/
├── domain/           # ドメインモデル・値オブジェクト
├── dto/              # データ転送オブジェクト
├── repository/       # データアクセス層
├── usecase/          # ユースケース層（アプリケーションロジック）
├── controller/       # REST APIエンドポイント
└── exception/        # カスタム例外
```

### データベース

```sql
-- 著者テーブル
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL CHECK (birth_date < CURRENT_DATE)
);

-- 書籍テーブル
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL CHECK (price >= 0),
    publication_status SMALLINT NOT NULL CHECK (publication_status IN (0, 1))
);

-- 書籍著者関連テーブル
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);
```
