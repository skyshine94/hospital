# hospital业务梳理

一、项目概述

1.1 项目结构：
![image](https://user-images.githubusercontent.com/77769369/134840143-0ae7527d-608f-4bf9-b074-6ba1d5ea8b9f.png)


1.2 医院设置模块（后台系统）
1.	准备工作：
1)	构建service-hosp模块，创建配置类配置分页插件并加入@MapperScan扫描mapper。
2)	创建基础字段实体类，在isDeleted字段上加入@TableLogic启用逻辑删除。（model）
3)	创建Result统一返回结果类和ResultCodeEnum统一返回状态信息类。（common-util）
4)	引入MD5工具类。（service-util）
5)	创建swagger配置类（service-util），在需要使用swagger的所有模块的启动入口类加入@ComponentScan。
6)	创建HospitalException自定义异常类和GlobalExceptionHandler全局异常处理类。（common-util包）
7)	整合Logback，配置全局统一日志处理logback-spring.xml。（service-hosp）
2.	业务：
1)	获取所有医院设置：（service-hosp、HospitalSetController）
调用mp方法。
2)	逻辑删除医院设置：（service-hosp、HospitalSetController）
调用mp方法。
3)	分页带条件获取医院设置：（service-hosp、HospitalSetController）
调用mp方法。
4)	添加医院设置：（service-hosp、HospitalSetController）
调用MD5工具类生成签名密钥。
封装状态和签名密钥。
调用mp方法。
5)	根据id获取医院设置：（service-hosp、HospitalSetController）
调用mp方法。
6)	修改医院设置：（service-hosp、HospitalSetController）
调用mp方法。
7)	批量删除医院设置：（service-hosp、HospitalSetController）
调用mp方法。
8)	根据id锁定或解锁医院设置：（service-hosp、HospitalSetController）
根据id获取医院设置。
封装状态。
调用mp方法。
9)	根据id获取并发送签名密钥：（service-hosp、HospitalSetController）
根据id获取医院设置。
发送短信。
1.3 数据字典模块（后台系统）
1.	准备工作：
1)	构建service-cmn模块，创建配置类配置分页插件并加入@MapperScan扫描mapper。
2)	创建Dict数据字典实体类，添加hasChildren字段，用于在前端显示层级关系。（model）
3)	引入依赖：redis、easyexcel。（service-cmn）
4)	创建DictListener监听器继承AnalysisEventListener：（service-cmn）
重写invoke方法，调用mp方法添加每行数据，用于导入数据。
5)	编写redis配置类继承CachingConfigurerSupport：（service-util）
加入@EnableCaching启用SpringCache缓存处理。
设置自定义keyGenerator规则。
设置RedisTemplate规则，配置序列化。
设置CacheManager缓存规则，配置序列化。
2.	业务：
1)	根据id查询子节点数据列表：（service-cmn、DictController）
调用mp方法根据上级id获取List集合。
创建私有方法，调用mp方法根据上级id获取记录数，判断是否存在子节点。
遍历List集合，调用私有方法判断是否存在子节点，封装hasChildren。
在方法上加入@Cacheable(value = "dict", keyGenerator = "keyGenerator")缓存返回结果。
2)	导出数据：（service-cmn、DictController）
设置导出表格的响应头信息。
调用mp方法获取List集合。
使用HttpServletResponse获取输出流。
调用EasyExcel.write方法。
3)	导入数据：（service-cmn、DictController）
调用MultipartFile获取输入流。
调用EasyExcel.read方法，在方法中传入DictListener监听器。
在方法上加入@CacheEvict(value = "dict", allEntries = true)清空指定缓存（用于更新或删除方法上）。
*@CachePut用于添加方法上。
1.4 数据接口模块（后台系统）
1.	准备工作：
1)	引入fastjson依赖，用于转换json数据。（common）
2)	引入HttpUtil工具类发送get和post请求。（service-util）
3)	编写HttpRequestHelper工具类：（service-util）
转换Map集合。
根据请求数据生成签名。
签名校验。
获取时间戳。
封装post请求。
4)	创建Mongo基础字段实体类。（model）
5)	创建HospitalRepository接口继承MongoRepository，在类上加入@Repository。(service-hosp包)
6)	创建DepartmentRepository接口继承MongoRepository，在类上加入@Repository。(service-hosp包)
7)	创建ScheduleRepository接口继承MongoRepository，在类上加入@Repository。(service-hosp包)
2.	业务：
1)	根据hoscode获取签名：（service-hosp、HospitalSetService）
调用mp方法。
2)	根据hoscode获取签名详情。（service-hosp、HospitalSetService）
调用mp方法。
封装url和签名密钥。
3)	上传医院接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
处理图片数据，将空格转换为加号，封装图片数据。
将Map集合转换成医院对象。
获取hoscode。
调用MongoRepository方法获取医院信息。
判断医院信息是否存在，处理对应数据。
调用MongoRepository方法。
4)	查询医院接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
调用MongoRepository方法。
5)	上传科室接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
将Map集合转换成科室对象。
获取hoscode和depcode。
调用MongoRepository方法获取科室信息。
判断科室信息是否存在，处理对应数据。
调用MongoRepository方法。
6)	分页根据hoscode查询科室接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
获取当前页和每页记录数。
创建Pageable和Example对象封装条件。
调用MongoRepository方法。
7)	删除科室接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
获取depcode。
调用MongoRepository方法根据hoscode和depcode获取科室信息。
调用MongoRepository方法。
8)	上传排班接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
将Map集合转换成科室对象。
获取hoscode和hosScheduleId。
调用MongoRepository方法获取排班信息。
判断排班信息是否存在，处理对应数据。
调用MongoRepository方法。
9)	分页带条件查询排班接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
获取当前页和每页记录数。
创建Pageable和Example对象封装条件。
调用MongoRepository方法。
10)	删除排班接口：（service-hosp、ApiController）
调用HttpServletRequest接收数据。
调用HttpRequestHelper转换Map集合。
获取医院端的加密签名和医院编号。
调用HospitalSetService根据hoscode获取签名。
加密签名，判断与医院端的加密签名是否一致。
获取hosScheduleId。
调用MongoRepository方法根据hoscode和hosScheduleId获取排班信息。
调用MongoRepository方法。
1.5 医院管理模块（后台系统）
1.	准备工作：
1)	引入nacos依赖，在需要使用nacos的所有模块的启动入口类上加入@EnableDiscoveryClient注册服务。
2)	引入feign依赖实现远程调用（service、service-client），在所有调用方的启动入口类上加入@EnableFeignClients。
3)	引入joda-time依赖，用于转换日期与星期。
4)	构建service-cmn-client模块，创建DictFeignClient接口并加入@FeignClient，在接口中引入service-cmn中需要被service-hosp调用的抽象方法。
2.	业务：
1)	根据dictcode和value名称获取name：（service-cmn、DictController）
判断dictcode是否为空。
为空则调用mp方法根据value获取数据字典信息。
不为空则调用mp方法根据dictcode获取数据字典信息，获取id作为上级id。
调用mp方法根据parentId和value获取数据字典信息。
2)	根据value名称获取name：（service-cmn、DictController）
将dictcode置为空字符串，根据dictcode和value名称获取name。
3)	分页带条件获取医院列表：（service-hosp、HospitalController）
创建Pageable和Example对象封装条件。
调用MongoRepository方法。
创建私有方法，调用DictFeignClient接口封装医院等级、省、市、地区名称。
获取list集合使用stream流调用私有方法封装医院等级、省、市、地区名称。
4)	更新医院上线状态：（service-hosp、HospitalController）
调用MongoRepository方法根据id获取医院信息。
封装状态和更新时间。
5)	获取医院详情：（service-hosp、HospitalController）
调用MongoRepository方法根据id获取医院信息。
调用私有方法封装医院等级、省、市、地区名称。
创建Map集合封装医院基本信息和预约规则。
6)	根据hoscode查询所有科室：（service-hosp、DepartmentController）
创建Example对象封装条件。
调用MongoRepository方法。
使用stream流根据大科室将List集合分组为Map集合。
遍历Map集合，获取小科室List集合，封装大科室。
遍历小科室List集合，将小科室封装到children字段中。
7)	根据hoscode获取医院名称：（service-hosp、HospitalService）
调用MongoRepository方法。
8)	根据hoscode和depcode分页获取排班信息：（service-hosp、ScheduleController）
创建Criteria对象，封装匹配条件。
创建Aggregation对象，根据workDate分组并排序，统计预约总数，进行分页。
调用MongoTemplate方法，返回List集合。
遍历List集合将日期转换成对应星期，封装星期。
创建Aggregation对象，根据workDate分组。
调用MongoTemplate方法，获取总记录数。
调用hospitalService根据hoscode获取医院名称。
封装List集合、总记录数和医院名称。
9)	根据hoscode、depcode和workDate获取排班信息：（service-hosp、ScheduleController）
调用MongoRepository方法，返回List集合。
创建私有方法，封装医院名称、科室名称、日期对应星期。
获取list集合使用stream流调用私有方法封装医院名称、科室名称、日期对应星期。
1.6 服务网关模块（后台系统）
1.	准备工作：
1)	构建service-gateway模块，在配置文件中配置网关。
2)	创建跨域配置类CorsWebFilter。（service-gateway）
3)	全局过滤器实现GlobalFilter, Ordered：（service-gateway）
获取请求路径。
调用AntPathMatcher匹配路径，返回结果。
1.7 用户管理模块（后台系统）
1.	准备工作：
1)	构建service-user模块，创建配置类配置分页插件并加入@MapperScan扫描mapper。
2.	业务：
1)	分页带条件获取用户列表：（service-user、UserController）
封装条件参数。
调用mp方法。	
创建私有方法，封装认证状态、锁定状态。
获取list集合使用stream流调用私有方法封装认证状态、锁定状态。
2)	用户锁定：（service-user、UserController）
调用mp方法根据userId获取用户信息。
封装状态。
调用mp方法。
3)	根据id获取用户详情：（service-user、UserController）
调用mp方法根据userId获取用户信息。
调用私有方法封装认证状态、锁定状态。
调用PatientService根据userId获取就诊人信息。
封装用户和就诊人信息。
4)	用户认证审批：（service-user、UserController）
判断认证状态是否为1或-1。
调用mp方法根据userId获取用户信息。
调用mp方法。
1.8 数据接口模块（前台系统）
1)	业务：
1)	分页带条件获取医院列表：（service-hosp、HospitalApiController）
创建Pageable和Example对象封装条件。
调用MongoRepository方法。
创建私有方法，调用DictFeignClient接口封装医院等级、省、市、地区名称。
获取list集合使用stream流调用私有方法封装医院等级、省、市、地区名称。
2)	根据hosname获取医院信息：（service-hosp、HospitalApiController）
调用MongoRepository方法。
3)	根据hoscode获取科室信息：（service-hosp、HospitalApiController）
创建Example对象封装条件。
调用MongoRepository方法。
使用stream流根据大科室将List集合分组为Map集合。
遍历Map集合，获取小科室List集合，封装大科室。
遍历小科室List集合，将小科室封装到children字段中。
4)	根据hoscode获取医院详情信息：（service-hosp、HospitalApiController）
调用MongoRepository方法根据id获取医院信息。
调用私有方法封装医院等级、省、市、地区名称。
创建Map集合封装医院基本信息和预约规则。
1.9 短信服务模块（前台系统）
1.	准备工作：
1)	构建service-msm模块，在启动入口类上加入@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)，关闭数据库自动配置。
2)	引入aliyun依赖，在配置文件中配置regionId、accessKeyId、secret。（service-msm）
3)	编写ConstantPropertiesUtil常量初始化工具类实现InitializingBean，用于在项目启动时，从配置文件中初始化常量值。（service-msm）
4)	引入生成随机验证码工具类。（service-msm）
2.	业务：
1)	发送手机验证码：（service-msm、MsmApiController）
从redis中获取验证码，如果获取到则直接返回。
调用随机验证码工具类生成验证码。
设置阿里云短信服务参数regionId、accessKeyId、secret。
设置模板code和模板中需要传入的参数。
发送短信。
判断是否发送成功，成功则将验证码放入redis中，设置有效时间为2分钟。
1.10 微信服务模块（前台系统）
1.	流程图：
 
2.	准备工作：
1)	引入HttpClient依赖。（service-user）
2)	引入jwt依赖。（common-util）
3)	编写JwtHelper工具类，生成token，根据token获取用户id和名称。（common-util）
4)	在配置文件中配置app_id、app_secret、redirect_url。（service-user）
5)	引入HttpClient工具类，用于模拟浏览器请求和响应的过程。（service-user）
6)	编写ConstantWxPropertiesUtil常量初始化工具类，用于在项目启动时，从配置文件中初始化常量值。（service-user）
3.	业务：
1)	生成微信二维码：（service-user、WeixinApiController）
创建Map集合封装appid、scope（值为snsapi_login）、redirect_uri（使用utf-8编码）、state（随机数）。
2)	根据openid获取用户信息：（service-user、UserInfoService）
调用mp方法。
3)	微信登录回调：（service-user、WeixinApiController）
接收微信端发送的授权临时票据code。
设置appid、secret、code参数，使用HttpClient请求微信固定地址。
从返回结果中解析openid和access_token。
调用UserInfoService根据openid获取用户信息。
判断用户信息是否存在，不存在则设置openid、access_token参数再次使用HttpClient请求微信固定地址。
从返回结果中解析用户昵称和头像。
调用mp方法，添加用户信息。
判断用户名称、昵称、手机号是否为空，将其设置为前端显示的name。
判断手机号是否为空，不为空则将openid设置为空，用于在前端判断用户是否已经绑定过手机。
调用jwt工具类生成token。
重定向到前端页面，请求中加入token、openid、name参数。
1.11 用户登录模块（前台系统）
1.	流程图：
 
2.	准备工作：
1)	编写AuthGlobalFilter全局过滤器类实现GlobalFilter和Ordered：（service-gateway）
创建AntPathMatcher对象匹配请求路径。
请求路径中有inner时不允许外部访问。
请求路径中有auth时从token中获取userId，判断userId是否存在。
不存在则不允许访问。
2)	构建service-oss模块，在启动入口类上加入@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)，关闭数据库自动配置。
3)	引入aliyun oss依赖。（service-oss包）
4)	在配置文件中配置endpoint、accessKeyId、secret、bucket。（service-oss）
5)	编写ConstantOssPropertiesUtil常量初始化工具类，用于在项目启动时，从配置文件中初始化常量值。（service-oss）
6)	编写AuthContextHolder获取当前用户工具类，根据token获取当前用户id和名称。（common-util）
3.	业务：
1)	用户登录：（service-user、UserInfoApiController）
从redis中获取验证码。
判断用户输入的验证码与redis中的验证码是否匹配。
判断是否存在openid，存在表示微信扫码后绑定手机。
创建私有方法，调用mp方法根据openid获取用户信息。
判断用户信息是否为空，不为空则绑定手机号。
调用mp方法更新用户信息。
openid不存在则表示手机登录。
调用mp方法根据phone获取用户信息。
判断用户信息是否为空，为空则添加用户信息。
判断用户是否被禁用。
判断用户名称、昵称、手机号是否为空，将其设置为前端显示的name。
调用jwt工具类生成token。
封装name和token。
2)	上传证件：（service-oss、FileApiController）
根据endpoint、accessKeyId、secret创建ossClient实例。
调用MultipartFile获取输入流。
调用MultipartFile获取文件名称，并加入uuid和当前日期组成服务器端文件存放位置。
调用ossClient.putObject方法上传文件。
关闭ossClient。
使用bucket、endpoint、文件路径生成服务器端文件完整url。
3)	用户认证：（service-user、UserInfoApiController）
调用mp方法根据userId获取用户信息。
封装用户证件信息。
调用mp方法更新用户信息。
1.12 就诊人管理模块（前台系统）
1.	业务：
1)	获取就诊人列表：（service-user、PatientApiController）
调用获取当前用户工具类获取userId。
调用mp方法根据userId获取就诊人信息。
创建私有方法调用DictFeignClient接口封装证件类型、联系人证件类型、省、市、区。
获取list集合使用stream流调用私有方法封装证件类型、联系人证件类型、省、市、区。
2)	添加就诊人：（service-user、PatientApiController）
调用获取当前用户工具类获取userId。
封装userId。
调用mp方法。
3)	根据id获取就诊人信息：（service-user、PatientApiController）
调用mp方法。
调用私有方法封装证件类型、联系人证件类型、省、市、区。
4)	修改就诊人信息：（service-user、PatientApiController）
调用mp方法。
5)	删除就诊人信息：（service-user、PatientApiController）
调用mp方法。
1.13 预约挂号模块（前台系统）
1.	业务：
1)	根据hoscode和depcode分页获取可预约排班数据：（service-hosp、HospitalApiController）
调用hospitalService根据hoscode获取预约规则。
获取预约周期。
创建私有方法将日期和时间转换成DateTime类型。
创建私有方法分页获取可预约日期信息，根据预约周期获取可预约日期List，创建分页List集合进行手动分页。
调用私有方法分页获取可预约日期信息。
创建Criteria对象，封装hoscode、depcode和可预约日期信息。
创建Aggregation对象，根据workDate分组，统计预约总数。
创建私有方法封装可预约日期、日期对应星期、预约状态。
获取List集合使用stream流调用私有方法封装可预约日期、日期对应星期、预约状态。
创建Map集合封装可预约排班规则List和其他基础数据Map。
2)	根据hoscode、depcode和workDate获取排班数据：（service-hosp、HospitalApiController）
调用MongoRepository方法。
获取list集合使用stream流调用私有方法封装医院名称、科室名称、日期对应星期。
3)	根据scheduleId获取排班数据：（service-hosp、HospitalApiController）
调用MongoRepository方法。
调用私有方法封装医院名称、科室名称、日期对应星期。
1.14 订单模块（前台系统）
1.	流程图：
 
 
 

2.	准备工作：
1)	构建service-user-client模块，创建PatientFeignClient接口并加入@FeignClient，引入service-user中需要被service-order调用的方法。
2)	构建service-hosp-client模块，创建HospitalFeignClient接口并加入@FeignClient，引入service-hosp中需要被service-order调用的方法。
3)	引入rabbitmq依赖。（rabbitmq-util）
4)	引入rabbitmq消息转换器。（rabbitmq-util）
5)	引入微信支付依赖。（service-order）
6)	在resources文件夹中引入退款证书。（service-order）
7)	在配置文件中配置appid、partner、partnerkey、cert。（service-order）
8)	编写ConstantOrderPropertiesUtil常量初始化工具类，用于在项目启动时，从配置文件中初始化常量值。（service-order）
9)	编写rabbitmq参数常量类。（rabbitmq-util）
10)	编写发送消息类。（rabbitmq-util）
3.	业务：
1)	根据id获取就诊人信息：（service-user、PatientApiController）
调用mp方法。
调用私有方法封装证件类型、联系人证件类型、省、市、区。
2)	根据scheduleId获取预约下单数据：（service-hosp、HospitalApiController）
调用MongoRepository方法获取排班信息。
调用HospitalService根据hoscode获取预约规则信息。
封装退号时间、挂号开始时间、挂号结束时间、当天停止挂号时间。
3)	根据hoscode获取医院签名信息：（service-hosp、HospitalApiController）
调用HospitalSetService根据hoscode获取医院签名信息。
4)	使用rabbitMQ发送短信：（service-msm、MsmService）
调用MsmService方法发送短信。
5)	更新排班信息：（service-hosp、ScheduleService）
封装更新时间。
调用MongoRepository方法。
6)	创建消息监听器：（service-msm、MsmReceiver）
监听exchange.direct.msm。
调用MsmService方法发送短信。
7)	创建消息监听器：（service-hosp、HospitalReceiver）
监听exchange.direct.order。
调用ScheduleService根据scheduleId获取排班详情。
判断剩余预约数是否为空，根据结果调用ScheduleService更新排班。
调用发送消息类向exchange.direct.msm发送消息。
8)	创建订单：（service-order、OrderApiController）
调用PatientFeignClient接口获取就诊人信息。
调用HospitalFeignClient接口获取排班信息。
判断当前时间是否可以预约。
调用HospitalFeignClient接口获取签名信息。
封装订单信息。
调用mp方法添加订单信息。
创建Map集合封装订单、就诊人信息和签名。
调用HttpRequestHelper请求医院端接口进行预约挂号。
封装返回医院预约记录主键、取号时间、取号地点、服务费、订单状态等信息。
调用mp方法更新订单信息。
封装排班预约信息和短信信息。
调用发送消息类向exchange.direct.order发送消息。
9)	根据orderId查询订单详情：（service-order、OrderApiController）
调用mp方法。
创建私有方法封装订单状态。
调用私有方法封装订单状态。
10)	分页带条件查询订单列表：（service-order、OrderApiController）
调用获取当前用户工具类获取userId。
封装条件参数。
调用mp方法。
获取list集合使用stream流调用私有方法封装订单状态。
11)	获取所有订单状态：（service-order、OrderApiController）
调用订单状态枚举类方法。
12)	添加支付信息：（service-order、PaymentService）
调用mp方法根据orderId和支付类型查询订单。
判断订单是否存在。
存在则封装支付信息。
调用mp方法。
13)	生成微信支付二维码：（service-order、WeixinController）
从redis中获取数据，如果获取到则直接返回。
调用PaymentService添加支付信息。
创建Map集合封装微信端所需appid、partner、partnerKey、out_trade_no、total_fee等参数。
调用WXPayUtil.generateSignedXml将Map集合转换成xml。
使用HttpClient请求微信固定地址。
接收微信端发送的数据。
调用WXPayUtil.xmlToMap将xml转换成Map集合。
获取result_code和code_url。
创建Map封装返回结果，将Map添加到redis中。
14)	更新订单状态：（service-order、PaymentService）
从Map集合中获取out_trade_no和微信支付订单号transaction_id。
根据out_trade_no和支付类型获取支付信息。
封装参数。
调用mp方法更新支付信息。
获取orderId。
调用mp方法根据orderId获取订单信息。
封装订单状态。
调用mp方法更新订单信息。
调用HospitalFeignClient接口获取医院签名。
封装信息。
调用HttpRequestHelper请求医院端更新支付信息。
15)	查询支付状态：（service-order、WeixinController）
调用mp方法根据orderId获取订单信息。
创建Map集合封装微信端所需appid、partner、out_trade_no等参数。
调用WXPayUtil.generateSignedXml将Map集合转换成xml。
使用HttpClient请求微信固定地址。
接收微信端发送的数据。
调用WXPayUtil.xmlToMap将xml转换成Map集合。
判断Map集合中的trade_state是否为SUCCESS。
是则调用PaymentService更新订单状态。
16)	根据orderId和支付类型获取支付信息：（service-order、PaymentService）
调用mp方法。
17)	添加退款信息：（service-order、RefundInfoService）
调用mp方法根据orderId和支付类型获取退款信息。
判断是否已存在退款信息。
存在则封装退款信息。
调用mp方法。
18)	微信退款：（service-order、WeixinService）
调用PaymentService根据orderId和支付类型获取支付信息。
调用RefundInfoService添加退款信息。
判断当前订单是否已经退款。
未退款则创建Map集合封装参数。
调用WXPayUtil.generateSignedXml将Map集合转换成xml。
设置证书信息使用HttpClient请求微信固定地址。
接收微信端发送的数据。
调用WXPayUtil.xmlToMap将xml转换成Map集合。
判断Map集合中的result_code是否为SUCCESS。
是则封装退款信息。
调用mp方法更新退款信息。
19)	取消预约：（service-order、OrderApiController）
调用mp方法获取订单信息。
获取退号时间。
判断是否可以取消预约。
是则调用HospitalFeignClient接口获取签名信息。
创建Map集合封装信息。
调用HttpRequestHelper请求医院端取消预约。
封装返回信息。
获取订单状态。
判断是否可以退款。
是则调用weixinService执行微信退款。
调用mp方法更新订单信息。
封装排班预约信息和短信信息。
调用发送消息类向exchange.direct.order发送消息。
1.15 就医提醒模块（前台系统）
1.	准备工作：
1)	构建service-task模块，在启动入口类上加入@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)，关闭数据库自动配置。
2.	业务：
1)	就诊通知：（service-order、OrderService）
调用mp根据安排日期和订单状态获取订单信息列表。
封装短信信息。
调用发送消息类向exchange.direct.msm发送消息。
2)	创建消息监听器：（service-order、OrderReceiver）
监听exchange.direct.task。
调用OrderService执行就诊通知。
3)	每天八点执行就医提醒：（service-task、ScheduleTask）
创建定时任务类，加入@EnableScheduling，在方法上加入@Scheduled(cron = "0 0 8 * * ?")。
调用发送消息类向exchange.direct.task发送消息。
1.16 预约统计模块（前台系统）
1.	准备工作：
1)	构建service-statistics模块，在启动入口类上加入@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)，关闭数据库自动配置。
2)	构建service-order-client模块，创建OrderFeignClient接口并加入@FeignClient，在接口中引入service-order中需要被service-statistics调用的抽象方法。
3)	在配置文件中引入打包xml资源的插件。（hospital包）
2.	业务：
1)	预约统计：（service-order、OrderApiController）
在Mapper文件中编写sql语句根据安排日期范围和医院名称查询安排日期和记录数，并根据安排日期分组、排序。
调用mapper方法。
获取List集合使用stream流将安排日期转换成List集合。
获取List集合使用stream流将记录数转换成List集合。
封装安排日期集合和记录数集合。
2)	预约统计：（service-statistics、StatisticsController）
调用OrderFeignClient接口执行预约统计。
