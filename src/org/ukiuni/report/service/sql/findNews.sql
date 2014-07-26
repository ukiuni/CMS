(
 (
  select 'EVENT_FOLD' as event, a.id as accountId, a.name as accountName, a.iconUrl as accountIconUrl, r.key as reportKey, r.title as reportTitle, reporter.id as targetAccountId, reporter.name as targetAccountName, reporter.iconUrl as targetAccountIconUrl, f.createdAt as createdAt
  from fold as f, Report as r, Account as a, Account as reporter
  where f.reportkey = r.key and r.account_id = reporter.id and (f.ACCOUNT_ID = ? or a.id in (select follower_id from follow where follows_id = ?)) and f.status = 'created'
 )
 union all (
  select 'EVENT_REPORT' as event, a.id as accountId, a.name as accountName, a.iconUrl as accountIconUrl, r.key as reportKey, r.title as reportTitle, reporter.id as targetAccountId, reporter.name as targetAccountName, reporter.iconUrl as targetAccountIconUrl, r.createdAt as createdAt
  from Report as r, Account as a, Account as reporter
  where r.account_id = a.id and r.account_id = reporter.id and a.id in (select follower_id from follow where follows_id = ?) and r.status = 'published'
 )
 union all (
  select  'EVENT_FOLLOW' as event, a.id as accountId, a.name as accountName, a.iconUrl as accountIconUrl, null as reportKey, null as reportTitle, target.id as targetAccountId, target.name as targetAccountName, target.iconUrl as targetAccountIconUrl, f.createdAt as createdAt
  from Follow as f, Account as a, Account as target, (
   select targetF.id as id
   from Follow as targetF
   where targetF.createdAt = (
    select max(innerF.createdAt)
    from Follow as innerF 
    where innerF.follower_id = targetF.follower_id and innerF.follows_id = targetF.follows_id
   )
  ) as maxDates
  where f.follower_id = target.id and (f.follows_id = ? or f.follows_id in (
   select follower_id
   from follow
   where follows_id = ?
  )) and f.id = maxDates.id and f.status = 'created'
 ) 
) order by createdAt desc