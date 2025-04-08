package com.example.nyusyukkin;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NyusyukkinMapper {

	/**
	 * Retrieves deposit and withdrawal information.
	 * @return the list of deposit and withdrawal information
	 */
	@Select("""
			SELECT
			    SHITENNAME AS "shitenName",
			    KOKYAKUID AS "kokyakuId",
			    NYUSYUKKINKUBUN AS "nyusyukkinKubun",
			    KINGAKU AS "kingaku",
			    TORIHIKIBI AS "torihikibi"
			FROM
			    NYUSYUKKINTBL
			""")
	List<NyusyukkinData> selectNyusyukkinData();

	/**
	 * Deletes deposit and withdrawal information.
	 * @return the number of records deleted
	 */
	@Delete("""
			DELETE
			FROM
			    NYUSYUKKINTBL
			""")
	int deleteNyusyukkinData();

	/**
	 * Inserts a single deposit/withdrawal record.
	 * @param data the deposit/withdrawal information
	 * @return the number of records inserted
	 */
	@Insert("""
			INSERT INTO NYUSYUKKINTBL(
			    SHITENNAME,
			    KOKYAKUID,
			    NYUSYUKKINKUBUN,
			    KINGAKU,
			    TORIHIKIBI
			)
			VALUES(
			    #{shitenName},
			    #{kokyakuId},
			    #{nyusyukkinKubun},
			    #{kingaku},
			    #{torihikibi}
			)
			""")
	int insertNyusyukkinData(NyusyukkinData data);

	/**
	 * Inserts deposit/withdrawal records in batch.
	 * @param list the list of deposit/withdrawal data
	 * @return the number of records inserted
	 */
	@Insert("""
			<script>
			INSERT INTO NYUSYUKKINTBL (SHITENNAME, KOKYAKUID, NYUSYUKKINKUBUN, KINGAKU, TORIHIKIBI) VALUES
			<foreach collection='list' item='item' separator=','>
			(#{item.shitenName}, #{item.kokyakuId}, #{item.nyusyukkinKubun}, #{item.kingaku}, #{item.torihikibi})
			</foreach>
			</script>
			""")
	int insertNyusyukkinDataBatch(List<NyusyukkinData> list);

}
