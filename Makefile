.PHONY: run gen-jooq

run:
	./gradlew bootRun

gen-jooq:
	./gradlew jooqCodegen
