// package andrehsvictor.dotask.aspect;

// import java.util.Arrays;

// import org.aspectj.lang.JoinPoint;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.AfterThrowing;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.stereotype.Component;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Aspect
// @Component
// public class LoggingAspect {

//     /**
//      * Pointcut para todos os controllers
//      */
//     @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
//     public void controllerPointcut() {
//         // Pointcut
//     }

//     /**
//      * Pointcut para todos os serviços
//      */
//     @Pointcut("within(@org.springframework.stereotype.Service *)")
//     public void servicePointcut() {
//         // Pointcut
//     }

//     /**
//      * Pointcut para todos os repositórios
//      */
//     @Pointcut("within(@org.springframework.stereotype.Repository *)")
//     public void repositoryPointcut() {
//         // Pointcut
//     }

//     /**
//      * Registra o tempo de execução e os detalhes de métodos nos controllers
//      */
//     @Around("controllerPointcut()")
//     public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
//         log.info("Iniciando requisição: {}.{}() com argumentos: {}",
//                 joinPoint.getSignature().getDeclaringTypeName(),
//                 joinPoint.getSignature().getName(),
//                 Arrays.toString(joinPoint.getArgs()));

//         long start = System.currentTimeMillis();
//         Object result = joinPoint.proceed();
//         long executionTime = System.currentTimeMillis() - start;

//         log.info("Requisição finalizada: {}.{}() em {} ms",
//                 joinPoint.getSignature().getDeclaringTypeName(),
//                 joinPoint.getSignature().getName(),
//                 executionTime);

//         return result;
//     }

//     /**
//      * Registra o tempo de execução e os detalhes de métodos nos serviços
//      */
//     @Around("servicePointcut()")
//     public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
//         if (log.isDebugEnabled()) {
//             log.debug("Executando serviço: {}.{}() com argumentos: {}",
//                     joinPoint.getSignature().getDeclaringTypeName(),
//                     joinPoint.getSignature().getName(),
//                     Arrays.toString(joinPoint.getArgs()));
//         }

//         long start = System.currentTimeMillis();
//         Object result = joinPoint.proceed();
//         long executionTime = System.currentTimeMillis() - start;

//         if (log.isDebugEnabled()) {
//             log.debug("Serviço finalizado: {}.{}() em {} ms",
//                     joinPoint.getSignature().getDeclaringTypeName(),
//                     joinPoint.getSignature().getName(),
//                     executionTime);
//         }

//         return result;
//     }

//     /**
//      * Registra o tempo de execução e os detalhes de métodos nos repositórios
//      */
//     @Around("repositoryPointcut()")
//     public Object logAroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
//         if (log.isTraceEnabled()) {
//             log.trace("Acessando repositório: {}.{}() com argumentos: {}",
//                     joinPoint.getSignature().getDeclaringTypeName(),
//                     joinPoint.getSignature().getName(),
//                     Arrays.toString(joinPoint.getArgs()));
//         }

//         long start = System.currentTimeMillis();
//         Object result = joinPoint.proceed();
//         long executionTime = System.currentTimeMillis() - start;

//         if (log.isTraceEnabled()) {
//             log.trace("Repositório finalizado: {}.{}() em {} ms",
//                     joinPoint.getSignature().getDeclaringTypeName(),
//                     joinPoint.getSignature().getName(),
//                     executionTime);
//         }

//         return result;
//     }

//     /**
//      * Registra detalhes de exceções lançadas por controllers, serviços e
//      * repositórios
//      */
//     @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut() || repositoryPointcut()", throwing = "e")
//     public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
//         log.error("Exceção em {}.{}() com causa = '{}'",
//                 joinPoint.getSignature().getDeclaringTypeName(),
//                 joinPoint.getSignature().getName(),
//                 e.getCause() != null ? e.getCause() : "NULL");

//         if (e.getCause() != null) {
//             log.error("Detalhes da exceção: ", e);
//         }
//     }
// }