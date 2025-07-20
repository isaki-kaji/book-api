.PHONY: setup db-up db-down migrate generate clean wait-for-db run migrate-after-run generate-after-run

run:
	./gradlew bootRun

# bootRun実行後に手動でマイグレーション実行
migrate-after-run:
	@echo "Docker起動済み環境でマイグレーションを実行しています..."
	./gradlew flywayMigrate

# bootRun実行後に手動でコード生成
generate-after-run:
	@echo "Docker起動済み環境でJOOQコードを生成しています..."
	./gradlew jooqCodegen

# 初期セットアップ（データベース起動、マイグレーション、コード生成）
setup: db-up wait-for-db migrate generate

# データベースの起動
db-up:
	@echo "データベースを起動しています..."
	docker compose up -d postgres

# データベース起動を確認
wait-for-db:
	@echo "データベースの起動を待機しています..."
	@for i in $$(seq 1 30); do \
		if docker compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then \
			echo "データベースが起動しました"; \
			break; \
		fi; \
		echo "データベースの準備中... ($$i/30)"; \
		sleep 1; \
	done

# データベースの停止
db-down:
	@echo "データベースを停止しています..."
	docker compose down

# Flywayマイグレーションを実行
migrate:
	@echo "データベースマイグレーションを実行しています..."
	./gradlew flywayMigrate

# JOOQコードを生成（マイグレーション後に実行）
generate:
	@echo "JOOQコードを生成しています..."
	./gradlew jooqCodegen

# クリーンアップ
clean:
	@echo "クリーンアップを実行しています..."
	./gradlew clean
	docker compose down
